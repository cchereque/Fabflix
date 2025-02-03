import React, {useEffect} from "react";
import {useUser} from "hook/User";
import styled from "styled-components";
import {useForm} from "react-hook-form";
import Idm from "backend/idm";
import {useNavigate} from "react-router-dom"


const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
`
const StyledDiv2 = styled.div`
  display: flex;
  flex-direction: row;
`

const StyledTd = styled.td`
    border: 1px solid black;
    text-align: left;
    padding: 15px
`


const StyledTh = styled.th`
    text-align: left;
    padding-left: 10px;
    padding-right: 5px;
`

const StyledTr = styled.tr`
    border: 1px solid black;
    padding: 25px;
`

const StyledTable = styled.table`
    text-align: left;
    border: 1px solid black;
    width: 100%;
    padding: 3px
`

const StyledP = styled.p`
    text-align: center;
`

const StyledH1 = styled.h1`
    font-size: 36px;
    text-align: center;
`

const StyledInput = styled.input`
`

const StyledButton = styled.button`
`

const OrderHistory = () => {

    const [sales, setSales] = React.useState([]);

    const getOrderHistory = () => {
        Idm.getOrderHistory(localStorage.getItem("access_token")).then(response => setSales(response.data["sales"]))
    }

    useEffect(() => {
        getOrderHistory()
    },[])

    return (
        <StyledDiv>
            <StyledH1>Order History</StyledH1>
            <br></br>
            <br></br>
            {sales &&
                <StyledTable>
                    <StyledTr>
                        <StyledTh>Id</StyledTh>
                        <StyledTh>Total</StyledTh>
                        <StyledTh>Date</StyledTh>
                    </StyledTr>
                    {sales.map (sale =>
                            <StyledTr>
                                <StyledTd>{sale.saleId}</StyledTd>
                                <StyledTd>{sale.total}</StyledTd>
                                <StyledTd>{sale.orderDate}</StyledTd>
                            </StyledTr>
                        )

                    }
                </StyledTable>
            }

            {!sales &&
                <StyledP>No Order History Found</StyledP>
            }
        </StyledDiv>
    );
}
export default OrderHistory;