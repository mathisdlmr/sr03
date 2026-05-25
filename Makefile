IMAGE_NAME:=mathisdlmr/devoir-2-sr03
IMAGE_TAG:=latest

default:
	cat ./Makefile
build:
	docker build -t $(IMAGE_NAME):$(IMAGE_TAG) .
run:
	docker run -p 8080:8080 $(IMAGE_NAME):$(IMAGE_TAG)
all: build run