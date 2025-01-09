
'use strict';

function isBlank(s) {
	return s === null || s === undefined || s === ""
}

const NavUsername = localStorage.getItem("nav_username")
const NavToken = localStorage.getItem("nav_token")

if (isBlank(NavUsername) || isBlank(NavToken)) {
	window.location.href = window.location.origin + "/login.html"
}

$.ajaxSetup({

	headers: {
		"Nav-Token": NavToken
	},

	error: function (xhr, textStatus, errorMsg) {
		if (xhr.status === 401) {
			window.location.href = window.location.origin + "/login.html"
		}
	}
})