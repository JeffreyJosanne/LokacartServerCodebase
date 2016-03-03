website.controller("ProductsCtrl",function($window, $scope, $http, $route, $location, ProductCreate, ProductEdit, ProductDelete) {
		
		var id;
		var image_url;
		var abbr = $('#organizationAbbr').val();
		var counter = 0, hot;
		var prodData;
		// Initialize the table
		$http.get( API_ADDR + 'api/products/search/productlist?abbr=' + abbr + '&projection=productproj').
			success(function(data, status, headers, config) {
				
				
				// Store the data into Angular scope model variable
				$scope.products = data._embedded.products;
			}).
			error(function(data, status, headers, config) {
				createAlert("Error Fetching Data","There was some error in response from the remote server.");
			});
		
		//function to save product
		$scope.saveProduct = function(data){
			$scope.newproduct = new ProductCreate();
			$scope.newproduct.name = data.name;
			$scope.newproduct.unitRate = data.unitRate;
		    $scope.newproduct.productType = data.productType;
		    $scope.newproduct.imageUrl= image_url;
		    $scope.newproduct.quantity = data.quantity;
		    console.log(image_url);
			ProductCreate.save($scope.newproduct,function(){
				$scope.products.push($scope.newproduct);
				createAlert("Success","The new Product was added Successfully.");
			},function(error){
				if (error.status == "409")
					createAlert("Error Adding Product","Product already added. Add a different product");
			});
		}
		
		$scope.uploadFile = function(data){
			if ($scope.myProductImage == undefined)
			{
				createAlert("Error Uploading File","No file added. Please choose a file of '.jpg' image format");
			}
			else if ($scope.myProductImage.type != "image/jpeg")
			{
				createAlert("Error Uploading File","Invalid File!! Please choose again. You can only choose a file of '.jpg' image format");
			}
			else
			{
				var formData=new FormData();
				formData.append("file",$scope.myProductImage); //myFile.files[0] will take the file and append in formData since the name is myFile.
				$http({
					method: 'POST',
					url: API_ADDR + 'web/'+abbr+'/uploadpicture', // The URL to Post.
					headers: {'Content-Type': undefined}, // Set the Content-Type to undefined always.
					data: formData,
					transformRequest: function(data, headersGetterFunction) {
						return data;
					}
				})
				.success(function(data, status) {
					console.log("Image successfully uploaded.");
					image_url=data;
					console.log(image_url);
					})
				.error(function(data, status) {
					createAlert("Error Uploading File","error "+status);
					if (status == "500")
						createAlert("Error Uploading File","File already present. Choose a different file before uploading.");
				});
			}
		}
		
		//add new product on clicking the add button
		$("#page-content").on("click", "#add-new-product", function(e) {
			e.preventDefault();
			var price = $.trim($('#new-price-input').val());
			var productType = $.trim($('#new-product-type-input').val());
			var product = $.trim($('#new-product-input').val());
			var quantity = $.trim($('#new-quantity-input').val());
			//org=$('#ProductLists').attr('org');
			if(! $.isNumeric(price)||price<0){
				createAlert("Invalid Input","Please enter valid price input as positive numerical value.");
			}
			else if(product == ''){
				createAlert("Invalid Input","Please enter a name for Product");
			}
			else if(productType == ""){
				createAlert("Invalid Input","Please select one of the Product Type(s)");
			}
			else if (! $.isNumeric(quantity)||quantity<0){
				createAlert("Invalid Input","Please enter valid quantity as positive numerical value.");
			}
			else
			{
				var data = {};
				data.name = product;
				data.unitRate = price;
				data.productType = productType;
				data.quantity = quantity;
				angular.element($('#add-new-product')).scope().uploadFile(data);
				setTimeout(function(){
					angular.element($('#add-new-product')).scope().saveProduct(data);
					},300);
				$('#add-product-modal').modal('toggle');
				$('#new-product-input').val("");
				$('#new-product-type-input').val("");
				$('#new-price-input').val("");
				$('#new-quantity-input').val("");
				angular.element($('#add-new-product')).scope().reload();
			}	
		});
		$("#page-content").on("click", "#add-new-product-multi", function (e) {
			e.preventDefault();
			var gridData = hot.getData();
			var flag=0;
			for (var i =0; i < gridData.length-1;++i)
			{
				var product = $.trim(gridData[i][0]);
				var price = $.trim(gridData[i][1]);
				var prodName = $.trim(gridData[i][2]);
				var quantity = $.trim(gridData[i][3]);
				var prodType;
				for (var x =0; x < prodData.products.length;++x)
					{
					if (prodData.products[x].name == prodName)
						{
						prodType = prodData.products[x].id;
						}
					}

				if (product && price && prodType && quantity)
				{
					if(! $.isNumeric(price)||price<0){
						flag=1;
					}
					else if(product == ''){
						flag=1;
					}
					else if (! $.isNumeric(quantity)||quantity<0) {
						flag=1;
					}
					else if(prodType == ""){
						flag=1;
					}
					else {
					var data = {};
					data.name = product;
					data.unitRate = price;
					data.productType="productTypes/"+prodType;		
					data.quantity = quantity;
					angular.element($('#add-new-product-multi')).scope().saveProduct(data);
					}
				}
			}
			
			$('#add-multiple-product-modal').modal('toggle');
			hot.clear();
		    if (flag == 1)
			{
				console.log("Flag: "+flag);
				createAlert("Invalid Input","Errors were present. Not all products were uploaded!");
			}
		    angular.element($('#add-new-product-multi')).scope().reload();	

		});
		
		$scope.displayStatus = function(product) {
			if (this.product.status == 1){
				return "Disable";
			}
			else {
				return "Enable";
			}
		}
		$scope.modifyClass = function(product) {
			if(this.product.status == 1){
				return "btn btn-danger btn-xs";
			}
			else if (this.product.status==0) {
				return "btn btn-success";
			}
		}
		
		$scope.editCurrentProductQuantity = function(product) {
			console.log("inside");
			$scope.id = this.product.productId;
			$scope.qty = parseFloat(this.product.quantity);
			$scope.updateQuantity = function() {
				console.log("processing");
				var incr =parseFloat($.trim($('#add-quantity-input').val()));
				var newQty = incr + $scope.qty;
				console.log("new quantity: "+newQty);
				$scope.editproduct = ProductEdit.get({id:$scope.id},function(){
					$scope.editproduct.quantity=newQty;
					$scope.editproduct.$update({id:$scope.id},function(){
						product.quantity = $scope.editproduct.quantity;
					});
				});
				$("#quantity-product-modal").modal('toggle');
				$('#add-quantity-input').val("");
			}
			
		}
		
		
		
		//function to edit product
		$scope.editCurrentProduct = function(product){
			
			$scope.id = this.product.productId;
			var stat=parseInt(this.product.status);
			$(".modal-header #HeadingEdit").html("Edit Product");
			$(".modal-body #update-product-input").html("Price");
			$(".modal-body #update-product-list-input-name").html("Name");
			$(".modal-body #update-quantity-input").html("Modify Quantity");
			if (stat == 1) {
				$(".modal-footer #toggle-product").html("Disable");
			}
			else if (stat == 0) {
				$(".modal-footer #toggle-product").html("Enable");
			}
			$("#update-product-name-input").val(this.product.name);
			$("#update-price-input").val(this.product.unitRate);
			$("#update-product-quantity-input").val(this.product.quantity);
			$scope.updateEditCurrentProduct = function() {
			
				newprice = $.trim($('#update-price-input').val());
				newname  = $.trim($('#update-product-name-input').val());
				newQty = $.trim($('#update-product-quantity-input').val());
				console.log("new qty: "+newQty);
				if(! $.isNumeric(newprice) && ! $.isNumeric(newQty)){
					createAlert("Invalid Input","Enter valid Price input and Quantity input as numerical value.");
				}
				else if(newprice < 0 && newQty < 0 )
				{
					createAlert("Negative Input not allowed.");
				}
				else
				{
					
					$scope.editproduct = ProductEdit.get({id:$scope.id},function(){
						$scope.editproduct.unitRate = newprice;
						$scope.editproduct.name = newname;
						$scope.editproduct.quantity = newQty;
						$scope.editproduct.$update({id:$scope.id},function(){
							product.unitRate = $scope.editproduct.unitRate;
							product.name = $scope.editproduct.name;
							product.quantity = $scope.editproduct.quantity;
						});
					});
					
					$("#edit-product-modal").modal('toggle');
					$('#update-price-input').val("");
					$('#update-product-name-input').val("");
				}
			
			}
			$scope.toggleCurrentProduct = function() {
				
				
				var currentStat = (stat+1)%2;
				$scope.editproduct = ProductEdit.get({id:$scope.id},function(){
					$scope.editproduct.status=currentStat;
					$scope.editproduct.$update({id:$scope.id},function(){
						product.status = $scope.editproduct.status;
					});
				});
				$("#edit-product-modal").modal('toggle');
			}	
		}
		
		$scope.enableDisableCurrentProduct= function(product){
			var stat=parseInt(this.product.status);
			var currentStat = (stat+1)%2;
			$scope.id = this.product.productId;
			$scope.editproduct = ProductEdit.get({id:$scope.id},function(){
				$scope.editproduct.status=currentStat;
				$scope.editproduct.$update({id:$scope.id},function(){
					product.status = $scope.editproduct.status;
				});
			});
		}
		
		$scope.globalEnable = function() {
			var abbr = $('#organizationAbbr').val();
			var toggleStatus=1;
			console.log(API_ADDR+'web/'+abbr+'/statusToggle?status='+toggleStatus)
			$http.get(API_ADDR+'web/'+abbr+'/statusToggle?status='+toggleStatus)
			.success(function (res) {
				$route.reload();
			})
			.error(function() {
				console.log("error");
			});	
		}
		
		$scope.globalDisable = function() {
			var abbr = $('#organizationAbbr').val();
			var toggleStatus=0;
			console.log(API_ADDR+'web/'+abbr+'/statusToggle?status='+toggleStatus)
			$http.get(API_ADDR+'web/'+abbr+'/statusToggle?status='+toggleStatus)
			.success(function (res) {
				$route.reload();
			})
			.error(function() {
				console.log("error");
			});	
		}

		
		$scope.displaySpreadsheet = function() {
			if (counter == 0) {
			var stuff = [[]];
			var container = document.getElementById('spreadsheet');
			var names = [];
			var orgid = $('#organizationId').val();
			var abbr = $('#organizationAbbr').val();
			$http.get(API_ADDR+'web/'+abbr+'/prodtypes')
			.success(function(results){
				prodData = results;
				for (var i=0;i<results.products.length;++i)
					{
					names[i]=results.products[i].name;
					}
				hot = new Handsontable(container, {
					  data: stuff,
					  minRows: 10,
					  minCols: 4,
					  minSpareRows: 1,
					  rowHeaders: false,
					  colHeaders: ['Product Name','Price (Per Unit)', 'Product Type','Quantity'],
					  columns: [
					            {},
					            {type: 'numeric'},
					            {
					              type: 'dropdown',
					              source: names
					            },
					            {type: 'numeric'}
					            
					          ],
					  contextMenu: true,
					  colWidths :120
					  
				}); 
			})
			.error(function() {
			    console.log( "error" );
			});
			++counter;
			}	
		}
		
		//function to delete product
		$scope.deleteCurrentProduct = function(product){
			
			$scope.id = this.product.productId;
			
			$scope.yesButtonDeleteProduct = function()
			{
				$scope.deleteproduct = ProductDelete.get({id:$scope.id},function(){				
					$scope.deleteproduct.$update({id:$scope.id},function(){
						
						// refetch the data
						$http.get( API_ADDR + 'api/products/search/productlist?abbr=' + abbr + '&projection=productproj').
						success(function(data, status, headers, config) {
							
							
							// Store the data into Angular scope model variable
							$scope.products = data._embedded.products;
						}).
						error(function(data, status, headers, config) {
							createAlert("Error Fetching Data","There was some error in response from the remote server.");
						});
						
						$("#delete-product-modal").modal('toggle');
						
					},function(error){
						if(error.status == "409")
							createAlert("Error Deleting Product","You can't delete this product.");
					});
				});	
			}
		}
		
		//function to fetch list of products
		$scope.getList = function(){
			
			$http.get( API_ADDR + 'api/products/search/productlist?abbr=' + abbr + '&projection=productproj').
			success(function(data, status, headers, config) {
				
				
				// Store the data into Angular scope model variable
				$scope.products = data._embedded.products;
			}).
			error(function(data, status, headers, config) {
				createAlert("Error Fetching Data","There was some error in response from the remote server.");
			});
			
		}
		
		//TODO hard refresh has to be eliminated
		$scope.reload = function(){
			setTimeout($window.location.reload.bind(window.location),2000);
		}
});
function readURL(input) {
    if (input.files && input.files[0]) {
        var reader = new FileReader();

        reader.onload = function (e) {
            $('#productImage')
                .attr('src', e.target.result)
                .width(50)
                .height(50);
        };

        reader.readAsDataURL(input.files[0]);
    }
}

