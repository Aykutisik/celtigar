# RAG Service (Java/Spring Boot, Gradle) — common developer tasks.
# Run `make help` to list available targets.

.DEFAULT_GOAL := help

.PHONY: help run build test clean

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-10s\033[0m %s\n", $$1, $$2}'

run: ## Build and run the service (Ctrl+C to stop) — http://localhost:8081
	./gradlew bootRun

build: ## Compile and package into build/libs
	./gradlew build

test: ## Run all tests
	./gradlew test

clean: ## Remove build artifacts
	./gradlew clean
