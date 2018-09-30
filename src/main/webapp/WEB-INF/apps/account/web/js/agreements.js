hui.ui.listen({
  $click$accept: function() {
    hui.ui.request({
      url : '/acceptTerms',
      $success :function() {
        document.location.reload();
      }
    })
  }
})