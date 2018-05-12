app = routescoop-api
doc = docker-compose -f docker-compose.yml
doctest = $(doc) -f docker-compose.override.yml -f test.yml run --rm
doccompile = $(doc) -f compile.yml run --rm

.PHONY: up up-go down clean compile test

clean:
	cd routescoop-api && rm -rf target project/target project/project
	cd routescoop-web && rm -rf target project/target project/project
	
compile:
	$(doccompile) routescoop-api
	$(doccompile) routescoop-web

test:
	$(doctest) $(app)

up-go:
	$(doc) -f docker-compose.override.yml up --build -d
	
down:
	$(doc) -f docker-compose.override.yml down --rmi local
	
up: compile up-go
