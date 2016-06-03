package almoneya.http

import org.scalatest.FlatSpec

import scala.collection.SortedSet

class RouterTest extends FlatSpec {
    behavior of "An empty Router"

    it must "fail to match any path" in {
        val router = Router(SortedSet.empty)
        assert(router.route("/").isEmpty)
        assert(router.route("/a").isEmpty)
    }

    behavior of "A Router with one route to /accounts/list"

    it must "fail to match /list" in {
        val router = Router(SortedSet(Route("""^/accounts/list""".r)))
        assert(router.route("/list").isEmpty)
    }

    it must "match /accounts/list" in {
        val route = Route("""^/accounts/list""".r)
        val router = Router(SortedSet(route))
        assert(router.route("/accounts/list").contains(route))
    }

    behavior of "A Router with two routes: /accounts/list and /accounts/create"

    it must "match /accounts/list and return the correct route" in {
        val list = Route("""^/accounts/list""".r)
        val create = Route("""^/accounts/create""".r)
        val router = Router(SortedSet(list, create))
        assert(router.route("/accounts/list").contains(list))
    }

    it must "match /accounts/create and return the correct route" in {
        val list = Route("""^/accounts/list""".r)
        val create = Route("""^/accounts/create""".r)
        val router = Router(SortedSet(list, create))
        assert(router.route("/accounts/create").contains(create))
    }

    behavior of "A router with two routes on /accounts, one with POST and the other with GET"

    it must "match GET /accounts to the correct route" in {
        val list = Route("""^/accounts""".r, Set(Route.GET))
        val create = Route("""^/accounts""".r, Set(Route.POST))
        val router = Router(SortedSet(list, create))
        assert(router.route("/accounts", Route.GET).contains(list))
    }

    it must "match POST /accounts to the correct route" in {
        val list = Route("""^/accounts""".r, Set(Route.GET))
        val create = Route("""^/accounts""".r, Set(Route.POST))
        val router = Router(SortedSet(list, create))
        assert(router.route("/accounts", Route.POST).contains(create))
    }

    it must "match longer routes first, even when passed out-of-order" in {
        val list = Route("""/accounts""".r, Set(Route.GET))
        val search = Route("""/accounts/search""".r, Set(Route.GET))
        val routerLS = Router(SortedSet(list, search))
        assert(routerLS.route("/accounts/search").contains(search))
        assert(routerLS.route("/accounts").contains(list))

        val routerSL = Router(SortedSet(list, search))
        assert(routerSL.route("/accounts/search").contains(search))
        assert(routerSL.route("/accounts").contains(list))
    }
}

class RouteTest extends FlatSpec {
    behavior of "A Route with an assertion of method == GET and path == /accounts"

    it must "fail on a POST request" in {
        val route = Route("""^/accounts""".r, Set(Route.GET))
        assert(!route.accepts("/accounts", Route.POST))
    }

    it must "reject a GET on /bank-account-transactions" in {
        val route = Route("""^/accounts""".r, Set(Route.GET))
        assert(!route.accepts("/bank-account-transactions", Route.GET))
    }

    it must "accept a GET on /accounts" in {
        val route = Route("""^/accounts""".r, Set(Route.GET))
        assert(route.accepts("/accounts", Route.GET))
    }

    it must "anchor the regexp on it's own even if not specified in the Regexp" in {
        val route = Route("""/accounts""".r, Set(Route.GET))
        assert(route.accepts("/accounts", Route.GET))
        assert(!route.accepts("/api/accounts", Route.GET))
    }
}
