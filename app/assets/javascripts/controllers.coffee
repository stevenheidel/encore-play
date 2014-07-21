encoreApp = angular.module("encorePublic", ['infinite-scroll'])

encoreApp.controller "EventsCtrl", ($scope, $http, $attrs) ->
  id = $attrs.id
  pagination_size = 20

  $http.get("/api/v2/events/" + id + ".json").success (data) ->
    $scope.event = data

  $http.get("/api/v2/events/" + id + "/posts.json").success (data) ->
    $scope.allPosts = data.posts
    $scope.posts = $scope.allPosts.slice(0, pagination_size)

  $scope.loadMore = ->
    newLength = $scope.posts.length + pagination_size
    $scope.posts = $scope.allPosts.slice(0, newLength)

encoreApp.controller "PostsCtrl", ($scope, $http, $attrs) ->
  id = $attrs.id

  $http.get("/api/v2/posts/" + id + ".json").success (data) ->
    $scope.post = data
    event_id = $scope.post.event_id

    $http.get("/api/v2/events/" + event_id + ".json").success (data) ->
      $scope.event = data