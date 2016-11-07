
var mainApp = angular.module('mainModule',[]);


mainApp.controller('mainCtrl', ['$scope','$rootScope','$http', function($scope,$rootScope,$http) {
	$rootScope.serviceServerURL="http://192.168.1.152:3000/service/";
	
  $scope.pageTitle = "Welcome to OpenHearts";
  $scope.pageHeading="Open Hearts";
  $scope.pageDescription="Open Hearts is open for your buddys";
  $scope.searchSnippet="lets look angels   ";
  $scope.searchButtonValue="Search";
  
  

  
  $scope.searchService= function (){
	  var serviceName="serviceinfo";
	  $http.get($rootScope.serviceServerURL+serviceName+"/"+$scope.searchbox)
	  .then(function(response){ 
		
		  $scope.searchResults = response.data; 
		  $scope.searchResultsLength= $scope.searchResults.length;
		  
	  });


  };
}]);
  
  mainApp.controller('authCtrl', ['$scope','$rootScope','$http','$window' ,function($scope,$rootScope,$http,$window) {
		$rootScope.serviceServerURL="http://192.168.1.152:3000/";
		
	  $scope.pageTitle = "Welcome to OpenHearts";
	  $scope.pageHeading="Open Hearts";
	  $scope.pageDescription="Open Hearts is open for your buddys";
	  $scope.searchSnippet="lets look angels   ";
	  $scope.searchButtonValue="Search";
	  
	  $scope.isRegisterClicked=false;
	  $scope.displayLogin= function(){
		  $scope.isRegisterClicked=false;
		  
		  
	  }
	  $scope.displayRegister= function(){
		  $scope.isRegisterClicked=true;
		  
		  
	  }

	  
	  $scope.authenticateUser= function (){
		  var serviceName="authuser";
		  var req = {
				  method: 'POST',
				  url: $rootScope.serviceServerURL+serviceName,
				  headers: {
				    'Content-Type': "application/json"
				  },
				  data: { "email": $scope.userName,"password":$scope.password }
				 }

				 $http(req).then(function(response){
					 
					 if(response.data.status==1){
						 //Success Condition lets redirectto Index.hml
						 $rootScope.staus=response.data.status;
						 $rootScope.isLoggedin=true;
						 $window.location.href = '/dashbord.html';
						 
						
					 }else{						 
						 $rootScope.isLoggedin=false;
						 $rootScope.staus=response.data.status;
						 $rootScope.erroMessage=msg;						 
					 }
				 
				 }, function(response){
					 $rootScope.isLoggedin=false;
					 $rootScope.staus=0;
					 $rootScope.erroMessage="There is a error call the service";
					 
					 
				 });
				 };

				 
				 
				 $scope.registerUser= function (){
					  var serviceName="registeruser";
					  var req = {
							  method: 'POST',
							  url: $rootScope.serviceServerURL+serviceName,
							  headers: {
							    'Content-Type': "application/json"
							  },
							  data: { "email": $scope.userNameReg,"password":$scope.passwordReg }
							 }

							 $http(req).then(function(response){
								 
								 if(response.data.status==1){
									 //Success Condition lets redirectto Index.hml
									 $rootScope.staus=response.data.status;
									 $rootScope.isLoggedin=true;
									 $window.location.href = '/dashbord.html';
									 
									
								 }else{						 
									 $rootScope.isLoggedin=false;
									 $rootScope.staus=response.data.status;
									 $rootScope.erroMessage=msg;						 
								 }
							 
							 }, function(response){
								 $rootScope.isLoggedin=false;
								 $rootScope.staus=0;
								 $rootScope.erroMessage="There is a error call the service";
								 
								 
							 });
							 };

	
}]);