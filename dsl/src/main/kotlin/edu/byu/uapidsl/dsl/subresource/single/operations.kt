package edu.byu.uapidsl.dsl.subresource.single

import edu.byu.uapidsl.UApiMarker

@UApiMarker
class SingleSubOperationsInit<AuthContext, ParentId, ParentModel, SingleSubModel> {

  inline fun <reified InputModel> createOrUpdate(init: SingleSubCreateOrUpdateInit<AuthContext, ParentId, ParentModel, SingleSubModel, InputModel>.() -> Unit) {

  }

  inline fun delete(init: SingleSubDeleteInit<AuthContext, ParentId, ParentModel, SingleSubModel>.() -> Unit) {

  }

  inline fun read(init: SingleSubReadInit<AuthContext, ParentId, ParentModel, SingleSubModel>.() -> Unit) {

  }

}

@UApiMarker
class SingleSubReadInit<AuthContext, ParentId, ParentModel, SingleSubModel> {
  fun authorization(auth: SingleSubReadAuthorizer<AuthContext, ParentId, ParentModel, SingleSubModel>) {

  }

  fun handle(handler: SingleSubReadHandler<AuthContext, ParentId, ParentModel, SingleSubModel>) {

  }

}

@UApiMarker
class SingleSubCreateOrUpdateInit<AuthContext, ParentId, ParentModel, SingleSubModel, UpdateModel> {
  fun authorization(auth: SingleSubCreateOrUpdateAuthorizer<AuthContext, ParentId, ParentModel, SingleSubModel, UpdateModel>) {

  }

  fun handle(handler: SingleSubCreateOrUpdateHandler<AuthContext, ParentId, ParentModel, SingleSubModel, UpdateModel>) {

  }
}

@UApiMarker
class SingleSubDeleteInit<AuthContext, ParentId, ParentModel, SingleSubModel> {
  fun authorization(auth: SingleSubDeleteAuthorizer<AuthContext, ParentId, ParentModel, SingleSubModel>) {

  }

  fun handle(handler: SingleSubDeleteHandler<AuthContext, ParentId, ParentModel, SingleSubModel>) {

  }
}

interface SingleSubReadLoadContext<AuthContext, ParentId, ParentModel> {
  val authContext: AuthContext
  val parentId: ParentId;
  val parent: ParentModel
}

interface SingleSubReadContext<AuthContext, ParentId, ParentModel, SingleSubModel> {
  val authContext: AuthContext
  val parentId: ParentId
  val parent: ParentModel
  val resource: SingleSubModel
}

interface SingleSubCreateOrUpdateContext<AuthContext, ParentId, ParentModel, SingleSubModel, UpdateModel> {
  val authContext: AuthContext
  val parentId: ParentId
  val parent: ParentModel
  val input: UpdateModel
  val resource: SingleSubModel?
}

interface SingleSubDeleteContext<AuthContext, ParentId, ParentModel, SingleSubModel> {
  val authContext: AuthContext
  val parentId: ParentId
  val parent: ParentModel
  val resource: SingleSubModel
}

typealias SingleSubCreateOrUpdateHandler<AuthContext, ParentId, ParentModel, SingleSubModel, InputModel> =
  SingleSubCreateOrUpdateContext<AuthContext, ParentId, ParentModel, SingleSubModel, InputModel>.() -> Unit

typealias SingleSubDeleteHandler<AuthContext, ParentId, ParentModel, SingleSubModel> =
  SingleSubDeleteContext<AuthContext, ParentId, ParentModel, SingleSubModel>.() -> Unit

typealias SingleSubReadHandler<AuthContext, ParentId, ParentModel, SingleSubModel> =
  SingleSubReadLoadContext<AuthContext, ParentId, ParentModel>.() -> SingleSubModel?


typealias SingleSubReadAuthorizer<AuthContext, ParentId, ParentModel, SingleSubModel> =
  SingleSubReadContext<AuthContext, ParentId, ParentModel, SingleSubModel>.() -> Boolean

typealias SingleSubCreateOrUpdateAuthorizer<AuthContext, ParentId, ParentModel, SingleSubModel, InputModel> =
  SingleSubCreateOrUpdateContext<AuthContext, ParentId, ParentModel, SingleSubModel, InputModel>.() -> Boolean

typealias SingleSubDeleteAuthorizer<AuthContext, ParentId, ParentModel, SingleSubModel> =
  SingleSubDeleteContext<AuthContext, ParentId, ParentModel, SingleSubModel>.() -> Boolean

