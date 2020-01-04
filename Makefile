.PHONY: build test run

VERSION ?= snapshot

build:
	docker build -t "vmtranslator:$(VERSION)" .
	docker run -it -v `pwd`:/var/app -v /home/deon/.m2:/home/app/.m2 vmtranslator:$(VERSION) mvn package

test:
	docker run -it -v `pwd`:/var/app -v /home/deon/.m2:/home/app/.m2 vmtranslator:$(VERSION) mvn test

run:
	docker run -it -v `pwd`:/var/app -v /home/deon/.m2:/home/app/.m2 vmtranslator:$(VERSION) java -cp target/vmtranslator-1.0-SNAPSHOT.jar com.luckymacro.app.VMTranslator foo
