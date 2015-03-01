if ('registerElement' in document && 'createShadowRoot' in HTMLElement.prototype && 'import' in document.createElement('link') && 'content' in document.createElement('template')) {
    console.log("YEAY this browser has native support for webcomponents!");
} else {
    //console.log("NAY this browser does not support WC :|");
    document.write('<script src="../vendor/bower_components/webcomponentsjs/webcomponents.min.js"><\/script>');
}