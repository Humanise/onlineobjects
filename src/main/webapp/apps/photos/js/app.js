hui.control({
  'ready!'() {
  },
  'selector.select!'(e) {
    if (e.value.kind == 'ImageGallery') {
      imagesSource.changeParameter('gallery', e.value.value);
    } else {
      imagesSource.changeParameter('gallery', null);
    }
    foundation.submerge();
  },
  'viewToggle.valueChanged!'(e) {
    listView.setVisible(e.value == 'list')
    galleryView.setVisible(e.value == 'gallery')
  },
  'gallery.buildImageUrl!'(e) {
    var item = e.value.item;
    return this._buildUrl({id: item.id, width: e.value.width, height: e.value.height});
  },
  _buildUrl(params) {
    var scale = 2;
    return '/service/image/?id=' + params.id + '&width=' + (Math.ceil(params.width/100)*100*scale) + '&height=' + (Math.ceil(params.height/100)*100*scale);
  },
  'gallery.select!'(e) {
    var selection = e.source.getSelection()[0];
    var container = document.querySelector('#image');
    container.innerHTML = '';
    if (selection) {
      var src = this._buildUrl({id: selection.id, width: container.clientWidth, height: container.clientHeight});
      var img = hui.build('img', {src: src, parent: container, style: 'aspect-ratio: ' + selection.width + ' / ' + selection.height + '; object-fit: contain; width: 100%; height: 100%;'});
    }
    foundation.disposeOverlay();
  },
  'add.click!'(e) {
    adder.show({target: add});
    addForm.focus();
  }
})