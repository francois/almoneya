package almoneya.http

import almoneya.http.Route.HttpMethod

import scala.util.matching.Regex

case class Router(routes: Seq[Route]) {
    def route(pathInfo: String, method: HttpMethod = Route.GET): Option[Route] =
        routes.find(_.accepts(pathInfo, method))
}

case class Route(path: Regex, methods: Set[HttpMethod] = Route.ALL_HTTP_METHODS) {
    def accepts(pathInfo: String, method: HttpMethod = Route.GET): Boolean =
        methods.contains(method) && path.findPrefixMatchOf(pathInfo).isDefined
}

object Route {

    def methodToHttpMethod(method: String): Option[HttpMethod] = method.toLowerCase match {
        case "get" => Some(GET)
        case "post" => Some(POST)
        case "patch" => Some(PATCH)
        case "delete" => Some(DELETE)
        case _ => None
    }

    sealed trait HttpMethod

    case object GET extends HttpMethod

    case object POST extends HttpMethod

    case object PATCH extends HttpMethod

    case object DELETE extends HttpMethod

    val ALL_HTTP_METHODS: Set[HttpMethod] = Set(GET, POST, PATCH, DELETE)
}
