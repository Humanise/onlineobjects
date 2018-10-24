hui.on(['hui'], function() {
  var root = hui.find('.js-intro');
  if (!root) return;

  hui.on(root, 'tap', {
    "[href='#signup']" : function(node, e) {
      e.preventDefault();
      oo.signUp();
    },
    "[href='#login']" : function(node, e) {
      hui.stop(e);
      hui.ui.callSuperDelegates(window,'showLogin');
    }
  });
});