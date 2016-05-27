# Almoneya

## A double-entry accounting system

This is an exploration into alternative schemas, using different tools than what I use everyday.

* Implementation language: [Scala](http://scala-lang.org);
* Strongly typed domain objects (use TenantId instead of Int to represent tenant IDs, for example);
* Schema management: [Sqitch](http://sqitch.org/);
* Web server: [Jetty](http://www.eclipse.org/jetty;/)
* Patterns: [Repository Pattern](http://www.martinfowler.com/eaaCatalog/repository.html).

## How to start

At the moment, only the API is exposed. To play with the API, run the following commands, after installing
[VirtualBox](https://www.virtualbox.org/wiki/Downloads) and [Vagrant](https://www.vagrantup.com/).

```
vagrant up
vagrant ssh
sudo /usr/bin/cpan -i -f -T App::Sqitch DBD::Pg TAP::Parser::SourceHandler::pgTAP
sudo -u postgres createuser --createdb --superuser vagrant
sudo -u postgres createdb --owner vagrant --locale en_US.UTF-8 --encoding UTF-8 --template template0 vagrant
sqitch deploy
psql --command "INSERT INTO public.tenants(tenant_id) VALUES (default)"
psql --command "INSERT INTO public.users(tenant_id, surname, rest_of_name) VALUES (1, 'Me', NULL)"
# This BCrypt password hash is "francois"
psql --command "INSERT INTO credentials.user_userpass_credentials(user_id, username, password_hash) VALUES (1, 'me', '$2a$10$9mLW3xjnzSl2rYAZfOjyvuYmq31lI8ajZOfRVQn4y9YGCLkJJTxT6')"
psql --command "INSERT INTO public.accounts(tenant_id, account_name, account_kind) VALUES (1, 'Checking', 'asset')"
bin/sbt run
# Choose almoneya.http.ApiServer

# In another terminal window, run:
curl --silent --verbose --user me:francois http://localhost:8080/api/accounts/
```
