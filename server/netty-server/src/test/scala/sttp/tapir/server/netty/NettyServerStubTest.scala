package sttp.tapir.server.netty

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.effect.unsafe.implicits.global
import org.scalatest.BeforeAndAfterAll
import sttp.monad.{FutureMonad, MonadError}
import sttp.tapir.server.interceptor.CustomInterceptors
import sttp.tapir.server.netty.internal.CatsUtil.CatsMonadError
import sttp.tapir.server.tests.ServerStubInterpreterTest

import scala.concurrent.Future

class NettyFutureServerStubTest extends ServerStubInterpreterTest[Future, Any, NettyFutureServerOptions] {
  override def customInterceptors: CustomInterceptors[Future, NettyFutureServerOptions] = NettyFutureServerOptions.customInterceptors
  override def monad: MonadError[Future] = new FutureMonad()
  override def asFuture[A]: Future[A] => Future[A] = identity
}

class NettyCatsServerStubTest extends ServerStubInterpreterTest[IO, Any, NettyCatsServerOptions[IO]] with BeforeAndAfterAll {
  private val (dispatcher, shutdownDispatcher) = Dispatcher[IO].allocated.unsafeRunSync()

  override def customInterceptors: CustomInterceptors[IO, NettyCatsServerOptions[IO]] =
    NettyCatsServerOptions.customInterceptors[IO](dispatcher)
  override def monad: MonadError[IO] = new CatsMonadError[IO]()
  override def asFuture[A]: IO[A] => Future[A] = io => io.unsafeToFuture()

  override protected def afterAll(): Unit = shutdownDispatcher.unsafeRunSync()
}
