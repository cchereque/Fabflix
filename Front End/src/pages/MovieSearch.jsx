import React from "react";
import {useUser} from "hook/User";
import styled from "styled-components";
import {useForm} from "react-hook-form";
import Idm from "backend/idm";
import {useSearchParams, useNavigate} from "react-router-dom";


const StyledDiv = styled.div`
  display: flex;
  flex-direction: row;
`

const StyledH1 = styled.h1`
    font-size: 40px;
    text-align: center;
`

const StyledTd = styled.td`
    border: 1px solid black;
    text-align: left;
    padding: 15px
`

const StyledTh = styled.th`
    border: 1px solid black;
    text-align: left;
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

const StyledButton = styled.button`
    font-size: 12px;
`

const StyledInput = styled.input`
    width: 100px;
`
const StyledP = styled.p`
    padding: 2px;
`

const StyledSelect = styled.select`
    height: 25px;
    padding: 0px;
`


const MovieSearch = () => {
    const [movies, setMovies] = React.useState([]);
    const [page, setPage] = React.useState(1);
    const [searchParams, setSearchParams] = useSearchParams();
    const navigate = useNavigate();

    const {register, getValues, handleSubmit} = useForm();

    const getMovies = () => {
        const title = getValues("title").trim()
        const year = getValues("year").trim()
        const director = getValues("director").trim()
        const genre = getValues("genre").trim()
        const limit = getValues("limit")
        const orderBy = getValues("orderBy")
        const offset = page
        const direction = getValues("direction")

        const payload = {
            title: title !== "" ? title : undefined,
            year: year !== "" ? year : undefined,
            director: director !== "" ? director : undefined,
            genre: genre !== "" ? genre : undefined,
            limit: limit,
            orderBy: orderBy,
            page: offset,
            direction: direction
        };

        setSearchParams(payload);

        Idm.movieSearch(payload, localStorage.getItem("access_token")).then(response => setMovies(response.data["movies"]))
    };

    const addToCart = (id) => {
        const payload = {
            movieId: id,
            quantity: getValues(id.toString())
        };
        alert("Movie added to cart");
        Idm.cartInsert(payload, localStorage.getItem("access_token")).then(response => console.log("added to cart"))
    };


    return (
        <div>
            <StyledH1>Search For Movies</StyledH1><br></br>

            <StyledDiv>
                <StyledP>Title: <StyledInput{...register("title")}/></StyledP>

                <StyledP>Year: <StyledInput{...register("year")}/></StyledP>

                <StyledP>Director: <StyledInput{...register("director")}/></StyledP>

                <StyledP>Genre: <StyledInput {...register("genre")}/></StyledP>

                <StyledP>    </StyledP>

                <StyledSelect {...register("limit")}>
                    <option value={10}>10</option>
                    <option value={25}>25</option>
                    <option value={50}>50</option>
                    <option value={100}>100</option>
                </StyledSelect>

                <StyledP>  </StyledP>

                <StyledSelect {...register("orderBy")}>
                    <option value={"title"}>Title</option>
                    <option value={"year"}>Year</option>
                    <option value={"rating"}>Rating</option>
                </StyledSelect>

                <StyledP>  </StyledP>

                <StyledSelect {...register("direction")}>
                    <option value={"asc"}>Ascending</option>
                    <option value={"desc"}>Descending</option>
                </StyledSelect>

                <StyledP>  </StyledP>

                <StyledButton onClick={handleSubmit(getMovies)}>Search</StyledButton>

                <StyledP>  </StyledP>

                <StyledButton onClick={() => setPage(page > 1 ? page - 1 : 1)}>Prev</StyledButton>

                <StyledP>{page}</StyledP>
                <StyledButton onClick={() => setPage(page + 1)}>Next</StyledButton>
            </StyledDiv>

            {movies.length !== 0 &&
                <StyledTable>
                    <StyledTr>
                        <StyledTh>Title</StyledTh>
                        <StyledTh>Year</StyledTh>
                        <StyledTh>Director</StyledTh>
                        <StyledTh>Details</StyledTh>
                        <StyledTh>Quick Add</StyledTh>
                    </StyledTr>
                    {movies.map(movie =>
                        <StyledTr>
                            <StyledTd>{movie.title}</StyledTd>
                            <StyledTd>{movie.year}</StyledTd>
                            <StyledTd>{movie.director}</StyledTd>
                            <StyledTd><StyledButton onClick={() => navigate("/movie/" + movie.id)}>INFO</StyledButton></StyledTd>
                            <StyledTd>
                                <select {...register(movie.id.toString())}>
                                    <option value={1}>1</option>
                                    <option value={2}>2</option>
                                    <option value={3}>3</option>
                                    <option value={4}>4</option>
                                    <option value={5}>5</option>
                                    <option value={6}>6</option>
                                    <option value={7}>7</option>
                                    <option value={8}>8</option>
                                    <option value={9}>9</option>
                                    <option value={10}>10</option>
                                </select>
                                <StyledButton onClick={() => addToCart(movie.id)}> Add To Cart</StyledButton>
                            </StyledTd>
                        </StyledTr>
                    )}
                </StyledTable>
            }
            {movies.length !== 0 &&
                <StyledDiv>
                    <br></br>
                    <StyledP><StyledButton onClick={handleSubmit(getMovies)}>Get Movies!</StyledButton></StyledP>

                    <StyledP><StyledButton onClick={() => setPage(page > 1 ? page - 1 : 1)}>Prev</StyledButton></StyledP>

                    <StyledP>{page}</StyledP>

                    <StyledP><StyledButton onClick={() => setPage(page + 1)}>Next</StyledButton></StyledP>
                </StyledDiv>
            }
        </div>
    );
}

export default MovieSearch;