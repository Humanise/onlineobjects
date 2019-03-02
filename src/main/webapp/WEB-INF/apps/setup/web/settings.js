var controller = {
  $submit$formula : function() {
    var values = formula.getValues();
    if (values.password1!==values.password2) {
      hui.ui.msg.fail({text:'The two passwords are not equal'})
      formula.focus();
      return;
    }
    hui.ui.request({
      url : 'changeAdminPassword',
      parameters : {password:values.password1},
      $success : function() {
        formula.reset();
        hui.ui.msg.success({text:'The password is now changed'})
      },
      $failure : function() {
        hui.ui.msg.fail({text:'The password could not be changed'})
        formula.focus();
      }
    })
  },
  $click$flushCache : function() {
    hui.ui.request({
      url : 'flushCache',
      $success : function() {
        hui.ui.msg.success({text:'Flushed'})
      }
    })
  }
}