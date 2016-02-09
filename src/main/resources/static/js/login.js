function reset() {
	console.log("inside");
	var username = document.getElementById('username-input').value;
	var phonenumber = document.getElementById('number-input').value;
	var newpassword = document.getElementById('newpassword-input').value;
	var confirmpassword = document.getElementById('confirmpassword-input').value;
	if (username == null || username == "" || phonenumber == null || phonenumber == "" || newpassword == null || newpassword == "" || confirmpassword == null || confirmpassword == "") {
		alert("All fields must be entered");
	}
	console.log(username + " " + phonenumber);
	if (newpassword == confirmpassword) {
		var obj = {};
		console.log("inside");
		obj['email']=username;
		obj['phonenumber']=phonenumber;
		obj['newpassword']=newpassword;
		console.log(obj);
		/*xhttp.onreadystatechange = function() {
			  if (xhttp.readyState == 4 && xhttp.status == 200) {
				  alert(xhttp.responseText);
			  }
			};
		xhttp.open("POST", "http://ruralict.cse.iitb.ac.in/RuralIvrs/app/passwordrequest", true);
		xhttp.setRequestHeader("Content-type", "application/json");
		xhttp.send(obj);*/
		$.ajax({
		    url: 'http://ruralict.cse.iitb.ac.in/RuralIvrs/app/passwordrequest',
		    type: 'POST',
		    data: JSON.stringify(obj),
		    contentType: 'application/json; charset=utf-8',
		    dataType: 'json',
		    async: false,
		    success: function(msg) {
		        alert(msg);
		    }
		});
		
	}
	else {
		alert("The passwords do not match");
	}

	
	
}