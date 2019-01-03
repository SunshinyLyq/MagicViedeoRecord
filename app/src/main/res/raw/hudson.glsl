precision mediump float;
 
 varying mediump vec2 aCoord;
 
 uniform sampler2D vTexture;
 uniform sampler2D vTexture2; //blowout;
 uniform sampler2D vTexture3; //overlay;
 uniform sampler2D vTexture4; //map
 
 uniform float strength;
 
 void main()
 {
     vec4 originColor = texture2D(vTexture, aCoord);
     
     vec4 texel = texture2D(vTexture, aCoord);
     
     vec3 bbTexel = texture2D(vTexture2, aCoord).rgb;
     
     texel.r = texture2D(vTexture3, vec2(bbTexel.r, texel.r)).r;
     texel.g = texture2D(vTexture3, vec2(bbTexel.g, texel.g)).g;
     texel.b = texture2D(vTexture3, vec2(bbTexel.b, texel.b)).b;
     
     vec4 mapped;
     mapped.r = texture2D(vTexture4, vec2(texel.r, .16666)).r;
     mapped.g = texture2D(vTexture4, vec2(texel.g, .5)).g;
     mapped.b = texture2D(vTexture4, vec2(texel.b, .83333)).b;
     mapped.a = 1.0;
     
     mapped.rgb = mix(originColor.rgb, mapped.rgb, strength);

     gl_FragColor = mapped;
 }