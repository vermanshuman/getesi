/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights
 *          reserved. For licensing, see LICENSE.md or
 *          http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function(config) {
    // Define changes to default configuration here. For example:
    // config.language = 'fr';
    // config.uiColor = '#AADC6E';
    console.log("start config CKEditor");

    config.enterMode = CKEDITOR.ENTER_BR;
    config.disableNativeSpellChecker = false;
    config.allowedContent = true;
    config.tabSpaces = 4;
    
    config.language = 'it_IT';
    config.wsc_lang = 'it_IT';
    config.scayt_sLang = 'it_IT';
    config.scayt_defLan ='it_IT';
    config.defaultLanguage='it_IT';
    config.scayt_autoStartup = false;
    config.removePlugins = 'liststyle';
    config.contentsCss = ["body {font-size: 38px;}"];
    config.font_names =
        'Arial/Arial, Helvetica, sans-serif;' +
        'Comic Sans MS/Comic Sans MS, cursive;' +
        'Courier New/Courier New, Courier, monospace;' +
        'Georgia/Georgia, serif;' +
        'Lucida Sans Unicode/Lucida Sans Unicode, Lucida Grande, sans-serif;' +
        'Tahoma/Tahoma, Geneva, sans-serif;' +
        'Times New Roman/Times New Roman, Times, serif;' +
        'Trebuchet MS/Trebuchet MS, Helvetica, sans-serif;' +
        'Calibri/Calibri, Verdana, Geneva, sans-serif;' + /* here is your font */
        'Verdana/Verdana, Geneva, sans-serif';
    config.toolbar = [
        {
            name : 'basicstyles',
            groups : [ 'basicstyles', 'cleanup' ],
            items : [ 'Bold', 'Italic', 'Underline', 'Strike', 'Subscript',
                'Superscript', '-', 'RemoveFormat' ]
        },
        {
            name : 'paragraph',
            groups : [ 'list', 'indent', 'blocks', 'align', 'bidi' ],
            items : [ 'NumberedList', 'BulletedList', '-', 'Outdent',
                'Indent', '-', '-', 'JustifyLeft', 'JustifyCenter',
                'JustifyRight', 'JustifyBlock', '-' ]
        },
        {
            name : 'insert',
            items : [ 'Image', 'Table', 'HorizontalRule', 'SpecialChar' ]
        },
        {
            name : 'styles',
            items : [ 'Font', 'FontSize' ]
        },
        {
            name : 'colors',
            items : [ 'TextColor', 'BGColor' ]
        },
        {
            name : 'clipboard',
            groups : [ 'clipboard', 'undo' ],
            items : [ 'Cut', 'Copy', 'PasteText', 'PasteFromWord', '-',
                'Undo', 'Redo' ]
        }, {
            name : 'editing',
            groups : [ 'selection', 'spellchecker' ],
            items : [ 'Replace', '-', 'SelectAll', '-', 'foundeospellchecker' ]

        }, {
            name : 'source',
            items : [ 'Source' ]
        }];
    console.log("end config CKEditor");
};
