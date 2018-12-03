/** Based on code from MDN web docs tutorial
 * https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/Your_second_WebExtension
 */
let isLoggedIn = false;

class SignupController {
    constructor(username, password, password3, email) {
        this.username = username;
        this.password = password;
        this.passwordmatch = password3;
        this.email = email;
    }

    isValidSignup() {
        if (this.emptyFieldExists()) {
            Display.displaySignupError("All fields are mandatory.");
            return false;
        } else if (this.isPasswordInvalid()) {
            Display.displaySignupError("Password must have 5 or more characters.");
            return false;
        } else if (this.isEmailInvalid()) {
            Display.displaySignupError("Email must be in valid format.");
            return false;
        } else if (!this.doPasswordsMatch()) {
            Display.displaySignupError("Passwords must match.");
            return false;
        } else {
            return true;
        }
    }

    emptyFieldExists() {
        return ((this.username.length === 0) || (this.email.length === 0) || (this.password.length === 0)
            || (this.passwordmatch.length === 0));
    }

    /**
     * Tests if email is valid. Regex is taken from
     * https://jsfiddle.net/ghvj4gy9/embedded/result,js/
     * @returns {boolean}
     */
    isEmailInvalid() {
        const emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return !(emailRegex.test(this.email));
    }

    isPasswordInvalid() {
        return (this.password.length < 5);
    }

    doPasswordsMatch() {
        return (this.password === this.passwordmatch);
    }
}

class Display {
    static async displayMessage(response) {
        document.querySelector("#logout-content").insertAdjacentHTML("afterend", "<div id='notification'>" +
            Display.formatResponseMessage(response)+"</div>");
        await Display.refreshDisplay();
        setTimeout(function(){
            document.getElementById("notification").remove();
        }, 2000);
    }

    static formatResponseMessage(response) {
        return JSON.stringify(response.result).replace(/"/g, "");
    }

    static clearPopup() {
        const elem = document.querySelectorAll("#popup-content, #signup-content, #check-URL-content, " +
            "#error-content, #login-content, #logout-content", "signup-error-msg");
        elem.forEach(elem => {
            elem.classList.add("hidden");
        });
        document.getElementById("loading-icon").style.display ='none';
    }

    static displaySignupError(msg) {
        // Remove pre-existing error messages
        let elem = document.getElementById("signup-error-msg");
        while (typeof(elem) !== 'undefined' && elem != null) {
            elem.remove();
            elem = document.getElementById("signup-error-msg");
        }
        document.querySelector("#popup-content").insertAdjacentHTML("afterend", "<div id='signup-error-msg'>"+
            msg+"</div>");
    }

    static displayLoginForm() {
        Display.clearPopup();
        document.querySelector("#login-content").classList.remove("hidden");
    }

    static displaySignUpForm() {
        Display.clearPopup();
        document.querySelector("#signup-content").classList.remove("hidden");
    }

    static displayLoading() {
        Display.clearPopup();
        document.getElementById("loading-icon").style.display ='block';
    }

    /**
     * Checks if user is logged in by making sending an AJAX request to the server.
     * If the page redirects to /login, the user is not logged in, so the variable isLoggedIn
     * is set as false. Otherwise, the variable is set as true.
     * Then calls setDisplay() to refresh the display of the popup.
     */
    static async refreshDisplay() {
        // Send ajax request to attempt to access localhost:7000/
        let xhr = new XMLHttpRequest();

        xhr.onload = function(){
            // See if we are redirected to login page
            let responseURL = xhr.responseURL;
            isLoggedIn = !(responseURL.includes("login"));
            clearTimeout(loading);
            //alert(responseURL);
            Display.setDisplay();
        };

        xhr.open("GET", "http://localhost:7000/");
        xhr.send();

        document.getElementById("loading-icon").style.display ='block';
        const loading = setTimeout(function() {
            document.getElementById("loading-icon").style.display ='none';
            document.querySelector("#calendue-title").insertAdjacentHTML("afterend", "<div id='notification'>" +
                "Error: could not reach server.</div>");
        }, 5000);

    }

    /**
     * Sets the popup display. If user is not logged in, shows the sign up form. If user is logged in,
     * checks the URL and either displays the add course/assignment page or tells user that they
     * need to be on Gradescope to add assignments.
     */
    static setDisplay() {
        Display.clearPopup();
        if (isLoggedIn) {
            browser.tabs.query({currentWindow: true, active: true})
                .then((tabs) => {
                    if (tabs[0].url.toString().includes("gradescope")) {
                        document.querySelector("#popup-content").classList.remove("hidden");
                    } else {
                        document.querySelector("#check-URL-content").classList.remove("hidden");
                    }
                });
            document.querySelector("#logout-content").classList.remove("hidden");
        } else {
            document.querySelector("#signup-content").classList.remove("hidden");
        }
    }
}

/**
 * Listen for clicks on the buttons, and send the appropriate message to
 * the content script in the page.
 */
async function listenForClicks() {
    // Checks login each time extension is loaded and displays proper page
    await Display.refreshDisplay();

    // Event listener for clicks
    document.addEventListener("click", (e) => {
        /**
         * Get page content and send a "add-course" message to the content script in the active tab.
         */
        async function sendCourseInfoToContentScript(tabs) {
            Display.displayLoading();
            browser.tabs.sendMessage(tabs[0].id, {
                command: "add-course"
            }, function(response) {
                Display.displayMessage(response);
            });
        }

        /**
         * Get username and password and send a "create-account" message to the content script in the active tab.
         * Checks for validity of sign up.
         */
        function sendSignupInfoToContentScript(tabs) {
            const username = document.getElementById("username").value;
            const password = document.getElementById("password").value;
            const password3 = document.getElementById("password3").value;
            const email = document.getElementById("email").value;

            const account = new SignupController(username, password, password3, email);

            if (account.isValidSignup()) {
                Display.displayLoading();
                browser.tabs.sendMessage(tabs[0].id, {
                    command: "create-account",
                    username: username,
                    email: email,
                    password: password
                }, function (response) {
                    Display.displayMessage(response);
                });
            }
        }

        /**
         * Get username and password and send a "create-account" message to the content script in the active tab.
         * Checks for validity of sign up.
         */
        function sendLoginInfoToContentScript(tabs) {
            const username = document.getElementById("username2").value;
            const password = document.getElementById("password2").value;

            if (username.length === 0 || password.length === 0) {
                Display.displaySignupError("Username or password cannot be blank.")
            } else {
                Display.displayLoading();
                browser.tabs.sendMessage(tabs[0].id, {
                    command: "login",
                    username: username,
                    password: password
                }, function (response) {
                    Display.displayMessage(response);
                });
            }
        }

        function sendLogoutRequestToContentScript(tabs) {
            Display.displayLoading();
            browser.tabs.sendMessage(tabs[0].id, {
                command: "logout"
            }, function(response) {
                Display.displayMessage(response);
            });
        }

        /**
         * Log the error to the console.
         */
        function reportError(error) {
            console.error(`Error in executing script: ${error}`);
        }

        const clickedItem = e.target.classList;

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
            Display.displayLoginForm();
        }
        if (clickedItem.contains("sign-up-form")) {
            Display.displaySignUpForm();
        }
        if (clickedItem.contains("logout")) {
            browser.tabs.query({active: true, currentWindow: true})
                .then(sendLogoutRequestToContentScript)
                .catch(reportError);
        }
    });

    clickOnEnterKey("signup-form", "signup-button");
    clickOnEnterKey("login-form", "login-button");

    /**
     * Allows user to submit form by using the enter key. Calls click() on the desired button.
     * @param focusId       the id of the field being focused on
     * @param buttonId      the id of the desired button
     */
    function clickOnEnterKey(focusId, buttonId) {
        var input = document.getElementById(focusId);
        input.addEventListener("keyup", function(event) {
            // Cancel the default action, if needed
            event.preventDefault();
            // Number 13 is the "Enter" key on the keyboard
            if (event.keyCode === 13) {
                // Trigger the button element with a click
                document.getElementById(buttonId).click();
            }
        });
    }
}

/**
 * There was an error executing the script.
 * Display the popup's error message, and hide the normal UI.
 */
function reportExecuteScriptError(error) {
    Display.clearPopup();
    document.querySelector("#error-content").classList.remove("hidden");
    console.error(`Failed to execute calendue content script: ${error.message}`);
}

/**
 * When the popup loads, inject a content script into the active tab,
 * and add a click handler.
 * If we couldn't inject the script, handle the error.
 */
browser.tabs.executeScript({file: "/content_scripts/calendue.js"})
    .then(listenForClicks)
    .catch(reportExecuteScriptError);