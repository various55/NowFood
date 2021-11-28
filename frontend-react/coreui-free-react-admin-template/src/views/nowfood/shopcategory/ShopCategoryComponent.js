import { React, useState } from 'react'
import { useSelector } from 'react-redux';
import 'ag-grid-community/dist/styles/ag-grid.css';
import 'ag-grid-community/dist/styles/ag-theme-alpine.css';
import TableComponent from 'src/components/nowfood/TableComponent';
import CreateShopCategoryComponent from './CreateShopCategory';

const ShopCategoryComponent = () => {
    const baseUrl = useSelector((state) => state.baseUrl);
    const head = [
        {
            field: 'id',
            flex: 1,
            sort: null,
            resizable: true,
            hide: false,
            pinned: '',
        },
        {
            field: 'name',
            flex: 1,
            sort: null,
            resizable: true,
            hide: false,
            pinned: ''
        },
        {
            field: 'Action',
            width: 120,
            sortable: false,
            hide: false,
            resizable: false,
            pinned: 'right',
            cellRenderer: "cellActionComponent"
        }
    ]
    const [config,setConfig] = useState({
        url: baseUrl + 'api/shops/category-shop/search-adv',
        reLoadData:false
    })

    const [shopCategory, setShopCategory] = useState({
        data: {
            id: 0,
            code: '',
            name: ''
        },
        visible: false
    }
    )

    const openCreateForm = () => {
        setShopCategory({
            data: {
                id: 0,
                code: '',
                name: ''
            },
            visible: true
        })
    }

    const openEditForm = (data) => {
        console.log(data)
        setShopCategory({
            data: data,
            visible: true
        })
    }

    const changeVisible = (value) => {
        setShopCategory({
            ...shopCategory,
            visible: value
        })
    }

    return (
        <div>
            <TableComponent header={head} config={config} btnCreateClick={openCreateForm} editData={openEditForm} />
            (<CreateShopCategoryComponent changeVisible={changeVisible} data={shopCategory} />)
        </div>)
}
export default ShopCategoryComponent