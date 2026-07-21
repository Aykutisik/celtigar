package main

import (
	"context"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gorilla/websocket"

	"github.com/aykut/celtigar/gateway/internal/llm"
)

var upgrader = websocket.Upgrader{
	ReadBufferSize:  1024,
	WriteBufferSize: 1024,
	// Allow all origins during development.
	// Restrict this to the real client origin before going to production.
	CheckOrigin: func(r *http.Request) bool { return true },
}

type inboundMessage struct {
	Message string `json:"message"`
}

type outboundMessage struct {
	Type  string `json:"type"` // "token" | "done" | "error"
	Delta string `json:"delta,omitempty"`
}

func handleWS(provider llm.Provider) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		conn, err := upgrader.Upgrade(w, r, nil)
		if err != nil {
			log.Printf("upgrade error: %v", err)
			return
		}
		defer conn.Close()

		for {
			var in inboundMessage
			if err := conn.ReadJSON(&in); err != nil {
				log.Printf("connection closed: %v", err)
				return
			}

			ctx, cancel := context.WithTimeout(r.Context(), 30*time.Second)
			tokens := make(chan string)
			errCh := make(chan error, 1)

			go func() {
				errCh <- provider.Stream(ctx, in.Message, tokens)
			}()

			streamErr := streamTokens(conn, tokens)
			if streamErr != nil {
				cancel()
				return
			}

			if err := <-errCh; err != nil {
				_ = conn.WriteJSON(outboundMessage{Type: "error", Delta: err.Error()})
			} else {
				_ = conn.WriteJSON(outboundMessage{Type: "done"})
			}
			cancel()
		}
	}
}

// streamTokens writes every token coming from the provider to the WebSocket
// connection. Returns early if a write fails (e.g. the client disconnected).
func streamTokens(conn *websocket.Conn, tokens <-chan string) error {
	for tok := range tokens {
		if err := conn.WriteJSON(outboundMessage{Type: "token", Delta: tok}); err != nil {
			return err
		}
	}
	return nil
}

func main() {
	// Phase 1.5: a real LLM provider (OpenAI/Anthropic) goes here.
	provider := llm.NewEchoProvider()

	mux := http.NewServeMux()
	mux.HandleFunc("/ws", handleWS(provider))
	mux.HandleFunc("/healthz", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("ok"))
	})
	mux.Handle("/", http.FileServer(http.Dir("./testclient")))

	srv := &http.Server{
		Addr:    ":8080",
		Handler: mux,
	}

	go func() {
		log.Println("gateway listening on :8080 (test client: http://localhost:8080/)")
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("listen error: %v", err)
		}
	}()

	stop := make(chan os.Signal, 1)
	signal.Notify(stop, syscall.SIGINT, syscall.SIGTERM)
	<-stop

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	_ = srv.Shutdown(ctx)
	log.Println("gateway stopped")
}
