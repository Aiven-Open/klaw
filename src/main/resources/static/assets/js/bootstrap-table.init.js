var data = [
    {
        "name": "bootstrap-table",
        "stargazers_count": "526",
        "forks_count": "122",
        "description": "An extended Bootstrap table with radio, checkbox, sort, pagination, and other added features. (supports twitter bootstrap v2 and v3) "
    },
    {
        "name": "multiple-select",
        "stargazers_count": "288",
        "forks_count": "150",
        "description": "A jQuery plugin to select multiple elements with checkboxes :)"
    },
    {
        "name": "bootstrap-show-password",
        "stargazers_count": "32",
        "forks_count": "11",
        "description": "Show/hide password plugin for twitter bootstrap."
    },
    {
        "name": "blog",
        "stargazers_count": "13",
        "forks_count": "4",
        "description": "my blog"
    },
    {
        "name": "scutech-redmine",
        "stargazers_count": "6",
        "forks_count": "3",
        "description": "Redmine notification tools for chrome extension."
    }
];

$(function () {
    $('#smptable').bootstrapTable({
        data: data
    });
});


/*table column*/

function buildTable($el, cells, rows) {
    var i, j, row,
        columns = [],
        data = [];

    for (i = 0; i < cells; i++) {
        columns.push({
            field: 'field' + i,
            title: 'Cell' + i
        });
    }
    for (i = 0; i < rows; i++) {
        row = {};
        for (j = 0; j < cells; j++) {
            row['field' + j] = 'Row-' + i + '-' + j;
        }
        data.push(row);
    }
    $el.bootstrapTable('destroy').bootstrapTable({
        columns: columns,
        data: data
    });
}

$(function () {
    buildTable($('#clmtable'), 50, 50);
});

var $table = $('#exporttable')
var $remove = $('#remove')
var selections = []

function getIdSelections() {
    return $.map($table.bootstrapTable('getSelections'), function (row) {
        return row.id
    })

}

function responseHandler(res) {
    $.each(res.rows, function (i, row) {
        row.state = $.inArray(row.id, selections) !== -1
    })
    return res
}

function detailFormatter(index, row) {
    var html = []
    $.each(row, function (key, value) {
        html.push('<p><b>' + key + ':</b> ' + value + '</p>')
    })
    return html.join('')
}

function operateFormatter(value, row, index) {
    return [
        '<a class="like" href="javascript:void(0)" title="Like">',
        '<i class="fas fa-heart"></i>',
        '</a>  ',
        '<a class="remove" href="javascript:void(0)" title="Remove">',
        '<i class="fas fa-trash"></i>',
        '</a>'
    ].join('')
}

window.operateEvents = {
    'click .like': function (e, value, row, index) {
        alert('You click like action, row: ' + JSON.stringify(row))
    },
    'click .remove': function (e, value, row, index) {
        $table.bootstrapTable('remove', {
            field: 'id',
            values: [row.id]
        })
    }
}

function totalTextFormatter(data) {
    return 'Total'
}

function totalNameFormatter(data) {
    return data.length
}

function totalPriceFormatter(data) {
    var field = this.field
    return '$' + data.map(function (row) {
        return +row[field].substring(1)
    }).reduce(function (sum, i) {
        return sum + i
    }, 0)
}

function initTable() {
    $table.bootstrapTable('destroy').bootstrapTable({
        height: 550,
        locale: $('#locale').val(),
        icons: {
            paginationSwitchDown: 'far fa-caret-square-down',
            paginationSwitchUp: 'far fa-caret-square-up',
            refresh: 'fas fa-history',
            toggleOff: 'fas fa-toggle-off',
            toggleOn: 'fas fa-toggle-on',
            columns: 'fas fa-list',
            fullscreen: 'fas fa-expand',
            detailOpen: 'fas fa-plus-circle',
            detailClose: 'far fa-times-circle',
            export: 'fas fa-share-alt'
        },
        columns: [
            [{
                field: 'state',
                checkbox: true,
                rowspan: 2,
                align: 'center',
                valign: 'middle'
            }, {
                title: 'Item ID',
                field: 'id',
                rowspan: 2,
                align: 'center',
                valign: 'middle',
                sortable: true,
                footerFormatter: totalTextFormatter
            }, {
                title: 'Item Detail',
                colspan: 3,
                align: 'center'
            }],
            [{
                field: 'name',
                title: 'Item Name',
                sortable: true,
                footerFormatter: totalNameFormatter,
                align: 'center'
            }, {
                field: 'price',
                title: 'Item Price',
                sortable: true,
                align: 'center',
                footerFormatter: totalPriceFormatter
            }, {
                field: 'operate',
                title: 'Item Operate',
                align: 'center',
                events: window.operateEvents,
                formatter: operateFormatter
            }]
        ]
    })
    $table.on('check.bs.table uncheck.bs.table ' +
        'check-all.bs.table uncheck-all.bs.table',
        function () {
            $remove.prop('disabled', !$table.bootstrapTable('getSelections').length)

            // save your data, here just save the current page
            selections = getIdSelections()
            // push or splice the selections if you want to save all data selections
        })
    $table.on('expand-row.bs.table', function (e, index, row, $detail) {
        if (index % 2 === 1) {
            $detail.html('Loading from ajax request...')
            $.get('LICENSE', function (res) {
                $detail.html(res.replace(/\n/g, '<br>'))
            })
        }
    })
    $table.on('all.bs.table', function (e, name, args) {
        console.log(name, args)
    })
    $remove.click(function () {
        var ids = getIdSelections()
        $table.bootstrapTable('remove', {
            field: 'id',
            values: ids
        })
        $remove.prop('disabled', true)
    })
}

$(function () {
    initTable()
    $('#locale').change(initTable)
})

var $table1 = $('#tablemodal')

$(function () {
    $('#modalTable').on('shown.bs.modal', function () {
        $table1.bootstrapTable('resetView')
    })
})

var $table2 = $('#subtable')

function buildTable($el, cells, rows) {
    var i; var j; var row
    var columns = []
    var data = []

    for (i = 0; i < cells; i++) {
        columns.push({
            field: 'field' + i,
            title: 'Cell' + i,
            sortable: true
        })
    }
    for (i = 0; i < rows; i++) {
        row = {}
        for (j = 0; j < cells; j++) {
            row['field' + j] = 'Row-' + i + '-' + j
        }
        data.push(row)
    }
    $el.bootstrapTable({
        columns: columns,
        data: data,
        detailView: cells > 1,
        onExpandRow: function (index, row, $detail) {
            /* eslint no-use-before-define: ["error", { "functions": false }]*/
            expandTable($detail, cells - 1)
        }
    })
}

function expandTable($detail, cells) {
    buildTable($detail.html('<table></table>').find('table'), cells, 1)
}

$(function () {
    buildTable($table2, 8, 1)
})

var $table3 = $('#localetable')

$(function () {
    $table3.bootstrapTable()
    $table3.bootstrapTable('destroy').bootstrapTable({
        locale: $('#localetable').val(),
        icons: {
            paginationSwitchDown: 'far fa-caret-square-down',
            paginationSwitchUp: 'far fa-caret-square-up',
            refresh: 'fas fa-history',
            toggleOff: 'fas fa-toggle-off',
            toggleOn: 'fas fa-toggle-on',
            columns: 'fas fa-list',
            fullscreen: 'fas fa-expand',
            detailOpen: 'fas fa-plus-circle',
            detailClose: 'far fa-times-circle',
            export: 'fas fa-share-alt'
        }
    })
    $('#localetable').change(function () {
        $table3.bootstrapTable('refreshOptions', {
            locale: $(this).val()
        })
    })
})