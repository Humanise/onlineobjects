oo.Link = function(options) {
  this.options = options;
  this.element = hui.get(options.element);
  this.name = options.name;
  hui.ui.extend(this);
  this._attach();
}

oo.Link.create = function(options) {
  options.element = hui.build('a.oo_link', {href: '#', children: [hui.build('span', {text: options.text})]});
  return new oo.Link(options);
}

oo.Link.prototype = {
  _attach : function() {
    hui.listen(this.element,'click',this._onClick.bind(this));
  },
  _onClick : function(e) {
    hui.stop(e)
    if (this.options.confirm) {
      hui.ui.confirmOverlay({
        widget : this,
        text : this.options.confirm.text,
        okText : this.options.confirm.okText,
        cancelText : this.options.confirm.cancelText,
        onOk : this._click.bind(this)
      });
    } else {
      this._click();
    }
  },
  _click : function() {
    this.fire('click');
  }
};