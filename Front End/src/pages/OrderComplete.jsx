import styled from "styled-components";
import React, {useEffect} from "react";
import {useUser} from "hook/User";
import {useForm} from "react-hook-form";
import Idm from "backend/idm";
import {useNavigate} from "react-router-dom"

const StyledDiv = styled.div`
  display: flex;
  flex-direction: column;
`

const StyledP = styled.p`
    text-align: right;
`

const StyledH1 = styled.h1`
    font-size: 36px;
    text-align: center;
`

const StyledTd = styled.td`
    border: 1px solid black;
    text-align: left;
    padding: 15px
`


const StyledTh = styled.th`
    text-align: left;
    padding-left: 10px;
    padding=right: 10px;
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

const StyledInput = styled.input`
`

const StyledButton = styled.button`
    width: 50px;
`
const StyledButton1 = styled.button`
    width: 100px;
`

const OrderComplete = () => {

    const [sale, setSale] = React.useState([]);
    const navigate = useNavigate();

    const getOrderHistory = () => {
        Idm.getOrderHistory(localStorage.getItem("access_token")).then(response =>
        {
            setSale(response.data["sales"]);
            console.log("order history retrieved");
            console.log(sale[0]);
        })
    }

    const orderComplete = (paymentIntentId) => {
        Idm.completeOrder(paymentIntentId, localStorage.getItem("access_token")).then(response => {
            console.log("order complete");
        });
    }

    useEffect(() => {
        const paymentIntentId = new URLSearchParams(window.location.search).get(
            "payment_intent"
        )
        orderComplete(paymentIntentId);
        getOrderHistory();
    }, [])

    return (
        <StyledDiv>
            <StyledH1>Order Complete</StyledH1>
            <br></br>
            {sale[0] &&
                <StyledTable>
                    <StyledTr>
                        <StyledTh>Date</StyledTh>
                        <StyledTh>Amount Paid</StyledTh>
                    </StyledTr>
                    <StyledTr>
                        <StyledTd>{sale[0].orderDate}</StyledTd>
                        <StyledTd>{sale[0].total}</StyledTd>
                    </StyledTr>
                </StyledTable>
            }
            <br></br>
            <StyledButton onClick={() => navigate("/cart")}>Done</StyledButton>
        </StyledDiv>
    );
}

export default OrderComplete;