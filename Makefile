IMAGE_NAME:=mathisdlmr/devoir-2-sr03
IMAGE_TAG:=latest

.PHONY: all build run build-application run-application build-frontend run-frontend default

all: build run

build: build-application build-frontend

run:
	@echo "============================================="
	@echo "Frontend disponible sur http://localhost:5173"
	@echo "Backend disponible sur http://localhost:8080"
	@echo "============================================="
	@make run-application > /dev/null 2>&1 &
	@make run-frontend > /dev/null 2>&1

build-application:
	docker build -t $(IMAGE_NAME):$(IMAGE_TAG) .
run-application:
	docker run --rm -p 8080:8080 $(IMAGE_NAME):$(IMAGE_TAG)

build-frontend:
	cd frontend && npm ci --silent
run-frontend:
	cd frontend && npm run dev

default:
	cat ./Makefile