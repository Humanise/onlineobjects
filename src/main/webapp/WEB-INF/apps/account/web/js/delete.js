hui.ui.listen({
  $submit$confirmationForm : function(form) {
    var values = form.getValues();
    if (hui.isBlank(values.username)) {
      form.focus();
      return;
    }
    if (hui.isBlank(values.password)) {
      form.focus();
      return;
    }
    hui.ui.request({
      url : '/deleteAccount',
      parameters : {
        username : values.username,
        password : values.password
      },
      $success : function(response) {
        document.location.reload();
      },
      $failure : function() {
        hui.ui.msg.fail({text:'It failed!'});
      }
    })

  }
})