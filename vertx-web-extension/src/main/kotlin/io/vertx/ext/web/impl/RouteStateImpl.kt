package io.vertx.ext.web.impl

import io.vertx.ext.web.Route

class RouteStateImpl(route: Route) {

    private val state = (route as RouteImpl ).state()

     val path: String? = state.path
     val name: String? = state.name
     val order = state.order
     val enabled = state.isEnabled
     val methods = state.methods?.map { it.name() }
     val consumes = state.consumes?.map { it.rawValue() }
     val emptyBodyPermittedWithConsumes = state.isEmptyBodyPermittedWithConsumes
     val produces= state.produces?.map { it.rawValue() }
     val contextHandlers = state.contextHandlers?.map { it.toString() }
     val failureHandlers = state.failureHandlers?.map { it.toString() }
     val added = state.isAdded
     val pattern = state.pattern?.toString()
     val groups: List<String>? = state.groups
     val useNormalizedPath = state.isUseNormalizedPath
     val namedGroupsInRegex: Set<String>? = state.namedGroupsInRegex
     val virtualHostPattern= state.virtualHostPattern?.toString()
     val pathEndsWithSlash = state.isPathEndsWithSlash
     val exclusive = state.isExclusive
     val exactPath = state.isExactPath
}