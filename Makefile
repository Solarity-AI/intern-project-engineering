JAVA_HOME ?= /opt/homebrew/Cellar/openjdk@17/17.0.18/libexec/openjdk.jdk/Contents/Home
export JAVA_HOME

# ── Backend ──────────────────────────────────────────────
.PHONY: backend backend-build backend-test backend-stop

backend: ## Start backend (localhost:8080)
	cd backend && mvn spring-boot:run

backend-build: ## Build backend
	cd backend && mvn clean install

backend-test: ## Run backend tests
	cd backend && mvn clean test

backend-stop: ## Kill process on port 8080
	lsof -ti:8080 | xargs kill -9 2>/dev/null || true

# ── Frontend (React Native) ──────────────────────────────
.PHONY: frontend frontend-web frontend-install

frontend: ## Start Expo dev server
	cd mobile && npx expo start

frontend-web: ## Start Expo web
	cd mobile && npx expo start --web

frontend-install: ## Install frontend dependencies
	cd mobile && npm install

# ── iOS ──────────────────────────────────────────────────
.PHONY: ios ios-generate

ios-generate: ## Generate Xcode project
	cd ios && xcodegen generate

ios: ios-generate ## Open Xcode project
	open ios/ProductReview.xcodeproj

# ── All ──────────────────────────────────────────────────
.PHONY: install test help

install: frontend-install backend-build ## Install all dependencies

test: backend-test ## Run all tests

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
