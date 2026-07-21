package llm

import (
	"context"
	"strings"
	"time"
)

// EchoProvider is a fake implementation used to validate the WebSocket +
// streaming flow end to end before wiring up a real LLM provider. Phase 1.5
// replaces this with a real OpenAI/Anthropic Provider; since the interface
// stays the same, only one line in gateway/main.go changes.
type EchoProvider struct{}

func NewEchoProvider() *EchoProvider { return &EchoProvider{} }

func (p *EchoProvider) Stream(ctx context.Context, prompt string, out chan<- string) error {
	defer close(out)

	response := "You said: " + prompt
	words := strings.Fields(response)

	for _, w := range words {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case out <- w + " ":
			time.Sleep(80 * time.Millisecond) // simulates real token-by-token streaming
		}
	}
	return nil
}
