website.controller('ManageInboundCallsController', function($scope, $route, InboundCallReports) {
	$('#fromInboundReportsDate').datepicker({
		dateFormat: 'yy-mm-dd',
		changeMonth: true,
		changeYear: true
	});
	$('#toInboundReportsDate').datepicker({
		dateFormat: 'yy-mm-dd',
		changeMonth: true,
		changeYear: true
	});
      $scope.callReports = function(data){
      	$scope.incomingCallReports =  InboundCallReports.get(data);
	};
	$scope.mySplit = function(string, nb) {
	    $scope.array = string.split('T');
	    return $scope.result = $scope.array[nb];
	}
});
      
$("#page-content").on("click", "#submitBut", function(e) {
    e.preventDefault();
    var from= $('#fromInboundReportsDate').val();
    var to= $('#toInboundReportsDate').val();
    if(from=="") createAlert("Invalid Input","Please select(type) a valid From date in yyyy-mm-dd format");
    else if(to=="") createAlert("Invalid Input","Please select(type) a valid To date in yyyy-mm-dd format");
    else if(validatedate(from)==false)	createAlert("Invalid Input","Please select(type) a valid From date in yyyy-mm-dd format");
    else if(validatedate(to)==false)	createAlert("Invalid Input","Please select(type) a valid To date in yyyy-mm-dd format");
    else if(to<from)  createAlert("Invalid Input","To date should be ahead of From date!");
    else{
		var data={};
		data.org = $("#organizationId").val();
		data.fromDate=from;
		data.toDate=to;
		angular.element($('#submitBut')).scope().callReports(data);
    }
});