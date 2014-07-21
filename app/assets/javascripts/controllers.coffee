phonecatApp = angular.module("encorePublic", ['infinite-scroll'])

phonecatApp.controller "EventsCtrl", ($scope, $http, $attrs) ->
  id = $attrs.id

  $http.get("/api/v2/events/" + id + ".json").success (data, status, headers, config) ->
    $scope.event = data

  $http.get("/api/v2/events/" + id + "/posts.json").success (data, status, headers, config) ->
    $scope.allPosts = data.posts
    $scope.posts = $scope.allPosts.slice(0, 10)

  $scope.loadMore = ->
    newLength = $scope.posts.length + 10
    $scope.posts = $scope.allPosts.slice(0, newLength)
