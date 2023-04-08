containerize:
	echo "Building the project"
	sbt clean compile assembly
	mkdir -p build
	cp target/scala-3.2.2/server.jar build/server.jar
	docker build -t wolt-backend-assignment .

test:
	sbt test

run:
	docker run -p 8080:8080 wolt-backend-assignment