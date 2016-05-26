import React from "react"

export default class AccountsList extends React.Component {
  render() {
    return <div className="small-12 columns">
      <h1>Accounts List</h1>

      <table>
        <thead>
          <tr>
            <th>Code</th>
            <th>Name</th>
            <th>Kind</th>
          </tr>
        </thead>
        <tbody>
        {this.props.accounts.map((account, index) => {
          return <tr key={"" + index}>
            <td style={{textAlign: "right"}}>{account.code}</td>
            <td>{account.name}</td>
            <td>{account.kind}</td>
          </tr>
        })}
        </tbody>
      </table>
    </div>;
  }
}
