 precision mediump float;
 
 varying mediump vec2 aCoord;
 
 uniform sampler2D vTexture;
 uniform sampler2D vTexture2;  //edgeBurn
 uniform sampler2D vTexture3;  //hefeMap
 uniform sampler2D vTexture4;  //hefeGradientMap
 uniform sampler2D vTexture5;  //hefeSoftLight
 uniform sampler2D vTexture6;  //hefeMetal
 
 uniform float strength;

 void main()
{
    vec4 originColor = texture2D(vTexture, aCoord);
    vec3 texel = texture2D(vTexture, aCoord).rgb;
    vec3 edge = texture2D(vTexture2, aCoord).rgb;
    texel = texel * edge;
    
    texel = vec3(
                 texture2D(vTexture3, vec2(texel.r, .16666)).r,
                 texture2D(vTexture3, vec2(texel.g, .5)).g,
                 texture2D(vTexture3, vec2(texel.b, .83333)).b);
    
    vec3 luma = vec3(.30, .59, .11);
    vec3 gradSample = texture2D(vTexture4, vec2(dot(luma, texel), .5)).rgb;
    vec3 final = vec3(
                      texture2D(vTexture5, vec2(gradSample.r, texel.r)).r,
                      texture2D(vTexture5, vec2(gradSample.g, texel.g)).g,
                      texture2D(vTexture5, vec2(gradSample.b, texel.b)).b
                      );
    
    vec3 metal = texture2D(vTexture6, aCoord).rgb;
    vec3 metaled = vec3(
                        texture2D(vTexture5, vec2(metal.r, texel.r)).r,
                        texture2D(vTexture5, vec2(metal.g, texel.g)).g,
                        texture2D(vTexture5, vec2(metal.b, texel.b)).b
                        );
    
    metaled.rgb = mix(originColor.rgb, metaled.rgb, strength);

    gl_FragColor = vec4(metaled, 1.0);
}