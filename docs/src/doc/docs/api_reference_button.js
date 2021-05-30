document.addEventListener("DOMContentLoaded", function() {
    let nav = document.getElementsByClassName("md-header__inner md-grid")[0];
    let button = document.createElement("div")
    button.className = "md-header__api-ref"
    let href = document.createElement("a")
    href.target= "_blank"
    href.href = "/generated/index.html"
    href.title = "Go to KDocs"
    href.className = "md-api-ref"
    href.appendChild(document.createTextNode("API Reference"))
    button.appendChild(href)
    let last = nav.children[nav.children.length - 1]
    console.log(last)
    nav.insertBefore(button, last)
});
