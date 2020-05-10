.PHONY: build test run

# If the first argument is "run"...
ifeq (run,$(firstword $(MAKECMDGOALS)))
  # use the rest as arguments for "run"
  RUN_ARGS := $(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))
  # ...and turn them into do-nothing targets
  $(eval $(RUN_ARGS):;@:)
endif

# If the first argument is "drun"...
ifeq (drun,$(firstword $(MAKECMDGOALS)))
  # use the rest as arguments for "run"
  RUN_ARGS := $(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))
  # ...and turn them into do-nothing targets
  $(eval $(RUN_ARGS):;@:)
endif

VERSION ?= snapshot

build:
	docker build -t "vmtranslator:$(VERSION)" .
	docker run -it -v `pwd`:/var/app -v /home/deon/.m2:/home/app/.m2 vmtranslator:$(VERSION) mvn package

test:
	docker run -it -v `pwd`:/var/app -v /home/deon/.m2:/home/app/.m2 vmtranslator:$(VERSION) mvn test -e

run:
	docker run -it -v `pwd`:/var/app -v /home/deon/.m2:/home/app/.m2 -v `pwd`/../07:/var/07 vmtranslator:$(VERSION) java -cp target/vmtranslator-1.0-SNAPSHOT.jar com.luckymacro.app.VMTranslator ../07/StackArithmetic/$(RUN_ARGS)
#	../projects/07/StackArithmetic/SimpleAdd/SimpleAdd.vm

# /home/deon/luckymacro/coursera/nand2tetris/part2/projects/vmtranslator

dbuild:
	vmtranslator:$(VERSION) mvn package

dtest:
	mvn test -e

drun:
	docker run -it\
		-v `pwd`:/var/app\
		-v `pwd`/../../tools:/var/tools\
		-v /home/deon/.m2:/home/app/.m2\
		-v `pwd`/../07:/var/07\
		vmtranslator:$(VERSION)\
		java -cp target/vmtranslator-1.0-SNAPSHOT.jar com.luckymacro.app.VMTranslator resources/inputs/$(RUN_ARGS).vm
	mv resources/inputs/$(RUN_ARGS).asm resources/test
	../../tools/CPUEmulator.sh resources/test/$(RUN_ARGS).tst
