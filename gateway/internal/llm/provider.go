package llm

import "context"

// Provider is the common interface for different LLM providers (OpenAI,
// Anthropic, a local model). Once the LLM Router becomes its own service,
// this interface will be served over gRPC with the same contract — the
// Gateway's code won't need to change.
type Provider interface {
	// Stream writes the response to prompt into out, token by token, and
	// closes the channel when done. The caller only reads from the channel.
	Stream(ctx context.Context, prompt string, out chan<- string) error
}
