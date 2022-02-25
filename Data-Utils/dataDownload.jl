# This script serves as an example to show how the images and articles for the News Images data set can be obtained
# We use mostly UNIX tools and the Julia language
# you should be able to replicate the process with other tools

# We need a set of libraries:
# DataFrames, CSV to handle the tabular data
# Pipe to combine different functions
# JSON to process JSON Objects
# StringEncodings to assure UTF-8 output
using DataFrames
using CSV
using Pipe
using JSON
using StringEncodings

# function to determine the image name from the URL, iid in the dataset
# in order to refer to the image, we create a new file with the format:
# <iid>.<imagetype>
function getImageName(imageUrl::String, iid::Int64)
    x = splitpath(imageUrl) |> last
    res = string(iid) * match(r"\..*$", x).match
    res
end

# function to download the image
# we use the tool "aria2" to download the image
# independently of the tool used, we need to use the user agent "Mozilla 5.0"
# otherwise the webserver will not provide the image
function downloadImage(imageUrl::String, iid::Int64)
    filename = getImageName(imageUrl, iid)
    try
        run(`aria2c --user-agent="Mozilla 5.0" -o $filename $imageUrl`)
    catch
        @warn "could not download image for $(imageURL)"
    end
end

# function to download the article from the publisher
# note that the function creates a local HTML file
function downloadArticle(articleURL::String, aid::Int64)
    filename = "$(aid).html"
    try
        run(`aria2c --user-agent="Mozilla 5.0" -o $filename $articleURL`)
    catch
        @warn "could not download article for $(articleURL)"
    end
end

# function to extract the article's text
# first, we load the content
# second, we look for the articleBody
# third, we extract the text and unescape the unicode sequences
# finally, we return the first 256 characters
function extractText(aid::Int64)
    fileContent = read("$(aid).html", String)
    !occursin("articleBody", fileContent) && return nothing
    output = match(r"\"articleBody\":\"([^\"]+)\"", fileContent)[1] |> unescape_string
    return output
end

# next, we download the images and articles
# we assume that the data files are located in the directory ../Data/
# we iterate through all the data sets and 
# note that some images or articles might no longer be available
# it make sense to check afterwards for cases of missing images or articles
# in the later evaluation, these cases ought to be excluded
for batch in [
    "../Data/content2019-01-v3.txt",
    "../Data/content2019-02-v3.txt",
    "../Data/content2019-03-v3.txt",
    "../Data/content2019-04-v4.txt"
]
    println("processing batch $(batch)")
    # reading the data set into a dataframe object
    df = CSV.File(batch; delim="\t") |> DataFrame
    for row in eachrow(df)
        downloadImage(row.img, row.iid)
        downloadArticle(row.URL, row.aid)
        extractText(row.aid)
    end
end
