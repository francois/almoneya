# Almoneya

## A double-entry accounting system

This is an exploration into alternative schemas, using different tools than what I use everyday.

* Implementation languages: [Scala](http://scala-lang.org) and [Elm](http://elm-lang.org/);
* Strongly typed domain objects (use `TenantId` instead of `Int` to represent tenant IDs, for example);
* Schema management: [Sqitch](http://sqitch.org/);
* Web server: [Jetty](http://www.eclipse.org/jetty;/)
* Patterns: [Repository Pattern](http://www.martinfowler.com/eaaCatalog/repository.html).

## How to start

At the moment, only the API is exposed. To play with the API, run the following commands, after installing
[VirtualBox](https://www.virtualbox.org/wiki/Downloads) and [Vagrant](https://www.vagrantup.com/).

```
vagrant up
vagrant ssh
cd /vagrant
bin/bootstrap

bin/sbt test run

# In another terminal window, run:
curl --silent --user username:francois http://localhost:8080/api/accounts/ | jq --color-output . | less --RAW-CONTROL-CHARS
```

## Project Organisation

The `almoneya-api/` directory holds the actual API. This is a Scala project. The `almoneya-frontend/` directory holds the
frontend application, written in Elm. Each project has further information on how to build and maintain it.
