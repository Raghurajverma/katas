package controllers.filters

import controllers.{RequestWithAttributes, StackableFilter}
import play.api.libs.Crypto
import play.api.mvc.{Session, Cookie, Result}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

case class CsrfTokenException(message: String) extends Exception(message)

trait CsrfFilter extends StackableFilter {

  private def cookieName: String = "XSRF-TOKEN"

  private def headerName: String = "X-XSRF-TOKEN"

  abstract override def filter[A](request: RequestWithAttributes[A])(f: (RequestWithAttributes[A]) => Future[Result]): Future[Result] = {
    def continue = super.filter(request)(f)
    continue

//    if (isUnsafeMethod(request.method) && request.contentType.exists(isUnsafeContentType)) {
//      // unsafe request
//      if (isXhrRequest(request)) {
//        // CORS
//        continue
//      } else {
//        // CSRF Token
//        getTokenFromCookie(request) map { cookieToken =>
//          getTokenFromHeader(request) map { headerToken =>
//            if (cookieToken == headerToken) {
//              continue
//            } else {
//              throw new CsrfTokenException("CSRF Token")
//            }
//          } getOrElse {
//            throw new CsrfTokenException("CSRF Header Token")
//          }
//        } getOrElse {
//          throw new CsrfTokenException("CSRF Cookie Token")
//        }
//      }
//    } else if (getTokenFromCookie(request).isEmpty) {
//      super.filter(request)(f) map { result =>
//        val newToken = generateToken
//        val tokenCookie = Cookie(
//          cookieName,
//          newToken,
//          path = Session.path,
//          domain = Session.domain,
//          secure = false,
//          httpOnly = false)
//
//        result.withCookies(tokenCookie)
//      }
//    } else {
//      continue
//    }
  }

  val unsafeMethods = Set("POST")

  val unsafeContentTypes = Set("")

  def isUnsafeMethod(method: String): Boolean = {
    unsafeMethods.contains(method)
  }

  def isUnsafeContentType(contentType: String): Boolean = {
    unsafeContentTypes.contains(contentType)
  }

  def isXhrRequest(request: RequestWithAttributes[_]): Boolean = {
    request.headers.get("X-Requested-With").isDefined
  }

  def getTokenFromCookie(request: RequestWithAttributes[_]): Option[String] = {
    request.cookies.get(cookieName).map(_.value)
  }

  def getTokenFromHeader(request: RequestWithAttributes[_]): Option[String] = {
    request.headers.get(headerName)
  }

  // tokens

  def generateToken: String = {
    Crypto.generateSignedToken
  }

  def compareToken(a: String, b: String): Boolean = {
    Crypto.compareSignedTokens(a, b)
  }

}
