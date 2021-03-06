package almoneya.http

import java.sql.Connection
import javax.servlet.http.HttpServletRequest

import almoneya.TenantId
import almoneya.http.Route.HttpMethod
import com.wix.accord.Violation
import org.eclipse.jetty.server.Request

import scala.collection.SortedSet
import scala.util.matching.Regex

case class Router(routes: SortedSet[Route]) {
    def numberOfRoutes = routes.size

    def route(pathInfo: String, method: HttpMethod = Route.GET): Option[Route] =
        routes.find(_.accepts(pathInfo, method))
}

case class Route(path: Regex, methods: Set[HttpMethod] = Route.ALL_HTTP_METHODS, controller: Controller = NoopController, transactionalBehaviour: TransactionalBehaviour = TransactionRequired) {
    def accepts(pathInfo: String, method: HttpMethod = Route.GET): Boolean =
        methods.contains(method) && path.findPrefixMatchOf(pathInfo).isDefined

    def execute(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest)(implicit connection: Connection): Either[Iterable[Violation], AnyRef] =
        controller.handle(tenantId, baseRequest, request)
}

sealed trait TransactionalBehaviour

case object TransactionRequired extends TransactionalBehaviour

case object NoTransactionNeeded extends TransactionalBehaviour

object Route {
    implicit val routerOrdering = new Ordering[Route] {
        /**
          * An Ordering that returns routes in more specific routes first fashion.
          *
          * The "more specific" route is simply derived from the path's length: a longer path implies
          * a more specific path.
          *
          * @param x A [[Route]]
          * @param y A [[Route]]
          * @return -1 if x is a more specific path, 1 if y is a more specific path, 0 if they are equal.
          */
        override def compare(x: Route, y: Route): Int = {
            val xre = x.path.regex
            val yre = y.path.regex
            val pathCompare = yre.compare(xre)
            if (pathCompare == 0) {
                val methodsCompare = y.methods.size.compare(x.methods.size)
                if (methodsCompare == 0) {
                    val xindex = Seq(POST, PATCH, DELETE, GET).zipWithIndex.find { case (method, index) => x.methods.contains(method) }.map(_._2)
                    val yindex = Seq(POST, PATCH, DELETE, GET).zipWithIndex.find { case (method, index) => y.methods.contains(method) }.map(_._2)
                    (xindex, yindex) match {
                        case (Some(xidx), Some(yidx)) if xidx != yidx => if (yidx - xidx < 0) -1 else 1
                        case (None, Some(_)) => -1
                        case (Some(_), None) => 1
                        case (_, _) => 0
                    }
                } else {
                    methodsCompare
                }
            } else {
                pathCompare
            }
        }
    }

    def methodToHttpMethod(method: String): Option[HttpMethod] = method.toLowerCase match {
        case "get" => Some(GET)
        case "post" => Some(POST)
        case "patch" => Some(PATCH)
        case "delete" => Some(DELETE)
        case _ => None
    }

    sealed trait HttpMethod {
        def name: String
    }

    case object GET extends HttpMethod {
        override def name: String = "GET"
    }

    case object POST extends HttpMethod {
        override def name: String = "POST"
    }

    case object PATCH extends HttpMethod {
        override def name: String = "PATCH"
    }

    case object DELETE extends HttpMethod {
        override def name: String = "DELETE"
    }

    val ALL_HTTP_METHODS: Set[HttpMethod] = Set(GET, POST, PATCH, DELETE)
}

case object NoopController extends Controller {
    override def handle(tenantId: TenantId, baseRequest: Request, request: HttpServletRequest)(implicit connection: Connection): Either[Iterable[Violation], AnyRef] =
        throw new RuntimeException("TODO: implement this controller")
}
