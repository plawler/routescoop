app = routescoop
doc = docker-compose -f docker-compose.yml
doctest = docker-compose -f docker-compose.test.yml run --rm

.PHONY: up down clean test

clean:
	cd routescoop-api && rm -rf target project/target project/project
	cd routescoop-web && rm -rf target project/target project/project

test:
	$(doctest) routescoop-api
	$(doctest) routescoop-web

down:
	$(doc) down --rmi local

up:
	$(doc) up -d
