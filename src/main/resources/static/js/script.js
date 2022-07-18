console.log("this is script file");

//implementing toogle sidebar function
//using arrow
const toggleSidebar = () => {

	//closing sidebar
	if ($(".sidebar").is(":visible")) {

		//for closing sidebar
		
		//removing opening animation
		$(".sidebar").removeClass("sidebar_out")
		$(".content").removeClass("content_right")
		
		//adding closing animation
		$(".sidebar").addClass("slide_close")
		$(".content").addClass("content_full")
		
		
		$(".content").css("margin-left", "2%");
		
		//setting timeout to make display none after animation ends
		setTimeout(function() {
			$(".sidebar").css("display", "none");
		}, 1000);

	}
	//opening sidebar
	else {

		$(".sidebar").css("display", "block");
		
		//adding opening sidebar and content-right animations
		$(".sidebar").addClass("sidebar_out")
		$(".content").addClass("content_right")

		//removing closing animations		
		$(".sidebar").removeClass("slide_close")
		$(".content").removeClass("content_full")
		
		
		$(".content").css("margin-left", "20%");
		
		
	}

};

const search = () => {
	// console.log("Searching");

	let query = $("#search-input").val();
	
	if (query == '')
	{
		$(".search-result").hide();
	}
	else{
		//search
		//console.log(query);
		
		//sending request to server
		let url = `http://localhost:8084/search/${query}`;

		fetch(url).then(response => {
			return response.json();
		}).then(data => {

			//data
			// console.log(data);

			let text = `<div class="list-group">`;

			data.forEach((contact) => {
				text+=`<a href="/user/${contact.cId}/contact" class="list-group-item list-group-item-action"> ${contact.name} </a>`
			} )

			text+= `</div>`;

			$(".search-result").html(text);
		}) ;


		//if there is no such contact found then no need to show search-result
		if (!$.trim($(".list-group").text()))
		{
			$(".search-result").hide();
		}
		else
		{
			
			$(".search-result").show();
		}
	}

}