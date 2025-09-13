FilePond.registerPlugin(
    FilePondPluginFileEncode,
    FilePondPluginFileValidateSize,
    FilePondPluginImageExifOrientation,
    FilePondPluginImagePreview
);
const input = document.querySelector('input.filepond');
const filePondInstance = FilePond.create(input, {
    storeAsFile: true,
    labelIdle: '.فایل خود را در<span class="filepond--label-action">اینجا </span> آپلود کنید',
});
