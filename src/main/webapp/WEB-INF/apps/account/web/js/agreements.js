hui.ui.listen({
  $click$accept: function() {
    hui.ui.request({
      url : '/acceptTerms',
      message: {success: 'Accepted!', text:'It failed!'},
      $success :function() {
        document.location.reload();
      }
    })
  }
})