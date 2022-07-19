console.log("this is script file");

var mediaquery = window.matchMedia("(max-width: 600px)")
//implementing toogle sidebar function
//using arrow
const toggleSidebar = () => {

	//This if block is for media query below or equal to 600px
	if (mediaquery.matches)
	{
		//closing sidebar
//		if ($(".sidebar").is(":visible")) {
		if ($(".sidebar").css("display") == "block") {
	
			//for closing sidebar
			
			$('.sidebar').css("margin-left", "-80%");
	//		$(".content").css("margin-left", "2%");
	
			//setting timeout to make display none after animation/transition ends
			setTimeout(function() {
				$(".sidebar").css("display", "none");
			}, 1000);
	
		}
		//opening sidebar
		else {
	
			$(".sidebar").css("display", "block");
			
			setTimeout(function() {
		//		$(".content").css("margin-left", "20%");
				$(".sidebar").css("margin-left", "0%");
			}, 10);
		}
	}
	//This block is for large screens media queries above 600px viewport
	else
	{
		//closing sidebar
		if ($(".sidebar").is(":visible")) {
	
			//for closing sidebar
			
			/*THIS WAS FOR THE ANIMATION ATTRIBUTES - DISCARDED*/
			//removing opening animation
	//		$(".sidebar").removeClass("sidebar_out")
	//		$(".content").removeClass("content_right")
	//		
	//		//adding closing animation
	//		$(".sidebar").addClass("slide_close")
	//		$(".content").addClass("content_full")
			
			
			$('.sidebar').css("margin-left", "-18%");
			$(".content").css("margin-left", "2%");
	
			//setting timeout to make display none after animation/transition ends
			setTimeout(function() {
				$(".sidebar").css("display", "none");
			}, 1000);
	
		}
		//opening sidebar
		else {
	
			$(".sidebar").css("display", "block");
			
			
			/*THIS WAS FOR THE ANIMATION ATTRIBUTES - DISCARDED*/
			//adding opening sidebar and content-right animations
	//		$(".sidebar").addClass("sidebar_out")
	//		$(".content").addClass("content_right")
	//
			//removing closing animations		
	//		$(".sidebar").removeClass("slide_close")
	//		$(".content").removeClass("content_full")
			
			setTimeout(function() {
			$(".content").css("margin-left", "20%");
			$(".sidebar").css("margin-left", "0%");
			}, 10);
		}
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
//		let url = `http://localhost:8084/search/${query}`;
		let url = `https://aditya-smart-contact-manager.herokuapp.com/search/${query}`;

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