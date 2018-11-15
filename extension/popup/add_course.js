/** Based on code from MDN web docs tutorial
 * https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/Your_second_WebExtension
 */
var isLoggedIn = false;

/**
 * Listen for clicks on the buttons, and send the appropriate message to
 * the content script in the page.
 */
function listenForClicks() {
    checkLogin();   // note: currently this is only called once at the loading of the extension

    document.addEventListener("click", (e) => {

        /**
         * Get page content and send a "add-course" message to the content script in the active tab.
         */
        function sendCourseInfoToContentScript(tabs) {
            browser.tabs.sendMessage(tabs[0].id, {
                command: "add-course"
            }).then(function() {
                document.querySelector("#signup-content").insertAdjacentHTML("afterend", "<div id='course-added-notification'>Course/assignment successfully added!</div>");
                setTimeout(function(){
                    document.getElementById("course-added-notification").remove()
                }, 2000);
            });
        }

        /**
         * Get username and password and send a "create-account" message to the content script in the active tab.
         * Checks for validity of sign up.
         */
        function sendSignupInfoToContentScript(tabs) {
            var username = document.getElementById("username").value;
            var email = document.getElementById("email").value;
            var password = document.getElementById("password").value;

            if (isValidSignup(username, email, password)) {
                browser.tabs.sendMessage(tabs[0].id, {
                    command: "create-account",
                    username: username,
                    email: email,
                    password: password
                }).then(function () {
                    document.querySelector("#signup-content").insertAdjacentHTML("afterend", "<div id='signup-notification'>Sign up successful! Please click extension again to add course</div>");
                    setTimeout(function () {
                        document.getElementById("signup-notification").remove()
                    }, 1000);
                });
            }
        }

        /**
         * Get username and password and send a "create-account" message to the content script in the active tab.
         * Checks for validity of sign up.
         */
        function sendLoginInfoToContentScript(tabs) {
            var username = document.getElementById("username2").value;
            var password = document.getElementById("password2").value;

            browser.tabs.sendMessage(tabs[0].id, {
                command: "login",
                username: username,
                password: password
            }).then(function () {
                document.querySelector("#signup-content").insertAdjacentHTML("afterend", "<div id='signup-notification'>Login successful! Please click extension again to add course</div>");
                setTimeout(function () {
                    document.getElementById("signup-notification").remove()
                }, 1000);
            });
        }

        function isValidSignup(username, email, password) {
            // Remove pre-existing error messages
            var elem = document.getElementById("signup-error-msg")
            while (typeof(elem) != 'undefined' && elem != null) {
                elem.remove();
                elem = document.getElementById("signup-error-msg");
            }

            if (emptyFieldExists(username, email, password)) {
                document.querySelector("#signup-content").insertAdjacentHTML("afterend", "<div id='signup-error-msg'>All fields are mandatory.</div>");
                return false;
            } else if (isPasswordInvalid(password)) {
                document.querySelector("#signup-content").insertAdjacentHTML("afterend", "<div id='signup-error-msg'>Password must have 5 or more characters.</div>");
                return false;
            } else if (isEmailInvalid(email)) {
                document.querySelector("#signup-content").insertAdjacentHTML("afterend", "<div id='signup-error-msg'>Email must be in valid format.</div>");
                return false;
            } else {
                return true;
            }
        }

        function emptyFieldExists(username, email, password) {
            if ((username.length == 0) || (email.length == 0) || (password.length == 0)) {
                return true;
            } else {
                return false;
            }
            return false;
        }

        /**
         * Tests if email is valid. Regex is taken from
         * https://jsfiddle.net/ghvj4gy9/embedded/result,js/
         * @param email
         * @returns {boolean}
         */
        function isEmailInvalid(email) {
            var emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            if (emailRegex.test(email)){
                return false;
            } else {
                return true;
            }
        }

        function isPasswordInvalid(password) {
            if (password.length < 5) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * checks the URL and reloads the login page if the active tab is on gradescope.
         */
        function sendCheckURLToContentScript(tabs) {
            browser.tabs.sendMessage(tabs[0].id, {
                command: tabs[0].url
            }).then(function() {
                if (tabs[0].url.toString().includes("gradescope")) {
                    document.querySelector("#signup-content").classList.remove("hidden");
                    document.querySelector("#check-URL-content").classList.add("hidden");
                }
            });
        }

        /**
         * Log the error to the console.
         */
        function reportError(error) {
            console.error(`Could not add course: ${error}`);
        }

        var clickedItem = e.target.classList;

        if (clickedItem.contains("add-course")) {
            browser.tabs.query({active: true, currentWindow: true})
                .then(sendCourseInfoToContentScript)
                .catch(reportError);
        }
        if (clickedItem.contains("sign-up")) {
            browser.tabs.query({active: true, currentWindow: true})
                .then(sendSignupInfoToContentScript)
                .catch(reportError);
        }
        if (clickedItem.contains("login")) {
            browser.tabs.query({active: true, currentWindow: true})
                .then(sendLoginInfoToContentScript)
                .catch(reportError);
        }
        if (clickedItem.contains("login-form")) {
            displayLoginForm();
        }
        if (clickedItem.contains("sign-up-form")) {
            displaySignUpForm();
        }
        if (clickedItem.contains("reload")) {
            console.log("contained");
            browser.tabs.query({active: true, currentWindow: true})
                .then(sendCheckURLToContentScript)
                .catch(reportError);
        }
    });
}

/**
 * There was an error executing the script.
 * Display the popup's error message, and hide the normal UI.
 */
function reportExecuteScriptError(error) {
    clearPopup();
    document.querySelector("#error-content").classList.remove("hidden");
    console.error(`Failed to execute calendue content script: ${error.message}`);
}

function clearPopup() {
    var elem = document.querySelectorAll("#popup-content, #signup-content, #check-URL-content, #error-content, #login-content");
    elem.forEach(elem => {
        elem.classList.add("hidden");
    });
}

function displayLoginForm() {
    clearPopup();
    document.querySelector("#login-content").classList.remove("hidden");
}

function displaySignUpForm() {
    clearPopup();
    document.querySelector("#signup-content").classList.remove("hidden");
}

/**
 * Checks if user is logged in by making sending an AJAX request to the server.
 * If the page redirects to /login, the user is not logged in, so the variable isLoggedIn
 * is set as false. Otherwise, the variable is set as true.
 */
async function checkLogin() {
    // Send ajax request to attempt to access localhost:7000/
    var xhr = new XMLHttpRequest();

    xhr.onload = function(){
        // See if we are redirected to login page
        var responseURL = xhr.responseURL;
        if (responseURL.includes("login")) {
            isLoggedIn = false;

        } else {
            isLoggedIn = true;

        }
        setDisplay();
    };

    xhr.open("GET", "http://localhost:7000/");
    xhr.send();
}

/**
 * Basic function that currently reports if user is logged in or not upon.
 */
function setDisplay() {
    clearPopup();
    if (isLoggedIn) {
        browser.tabs.query({currentWindow: true, active: true})
            .then((tabs) => {
                if (tabs[0].url.toString().includes("gradescope")) {
                    document.querySelector("#popup-content").classList.remove("hidden");
                } else {
                    document.querySelector("#check-URL-content").classList.remove("hidden");
                }
            })
    } else {
        document.querySelector("#signup-content").classList.remove("hidden");
    }
}

/**
 * When the popup loads, inject a content script into the active tab,
 * and add a click handler.
 * If we couldn't inject the script, handle the error.
 */
browser.tabs.executeScript({file: "/content_scripts/calendue.js"})
    .then(listenForClicks)
    .catch(reportExecuteScriptError);