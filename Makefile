.PHONY: build test

VERSION ?= snapshot

build:
	docker build -t "vmtranslator:$(VERSION)" .

test:
	docker run -it -v `pwd`:/var/app -v /home/deon/.m2:/home/app/.m2 vmtranslator:$(VERSION) mvn test
