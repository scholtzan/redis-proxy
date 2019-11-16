.PHONY: test

test:
	docker-compose build
	docker-compose run app sbt test
	docker-compose down

build:
	docker-compose build

run:
	docker-compose build
	docker-compose up 
