# Gateway service — common developer tasks.
# Run `make help` to list available targets.

BINARY_NAME := gateway
BIN_DIR     := bin
BIN_PATH    := $(BIN_DIR)/$(BINARY_NAME)

# Default target when you just run `make`.
.DEFAULT_GOAL := help

.PHONY: help run build tidy fmt vet test clean

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-10s\033[0m %s\n", $$1, $$2}'

run: ## Build and run the gateway (Ctrl+C to stop)
	go run .

build: ## Compile a binary into bin/gateway
	go build -o $(BIN_PATH) .

tidy: ## Resolve and download dependencies
	go mod tidy

fmt: ## Format all Go source files
	go fmt ./...

vet: ## Report suspicious constructs
	go vet ./...

test: ## Run all tests
	go test ./...

clean: ## Remove build artifacts
	rm -rf $(BIN_DIR)
