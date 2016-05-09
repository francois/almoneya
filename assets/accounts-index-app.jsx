import React from "react"
import ReactDOM from "react-dom"
import AccountsIndex from "./components/AccountsIndex.jsx"

import './foundation.css'

ReactDOM.render(
    <AccountsIndex accounts={window.app.accounts} />,
    document.getElementById("root"))
